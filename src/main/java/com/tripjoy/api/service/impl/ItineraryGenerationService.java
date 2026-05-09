package com.tripjoy.api.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tripjoy.api.dto.ai.AiCoordinateDto;
import com.tripjoy.api.dto.ai.AiFinalItineraryDto;
import com.tripjoy.api.dto.ai.AiModifyItineraryRequestDto;
import com.tripjoy.api.dto.ai.AiSuggestLocationRequestDto;
import com.tripjoy.api.dto.ai.AiTravelRequestDto;
import com.tripjoy.api.dto.ai.AiTripItemDto;
import com.tripjoy.api.dto.ai.GooglePlaceDetailsDto;
import com.tripjoy.api.dto.request.AiModifyItineraryRequest;
import com.tripjoy.api.dto.request.AiSuggestLocationRequest;
import com.tripjoy.api.dto.request.GenerateItineraryRequest;
import com.tripjoy.api.dto.response.ItineraryResponse;
import com.tripjoy.api.dto.response.TripItemResponse;
import com.tripjoy.api.entity.Itinerary;
import com.tripjoy.api.entity.Location;
import com.tripjoy.api.entity.Theme;
import com.tripjoy.api.entity.TripItem;
import com.tripjoy.api.entity.User;
import com.tripjoy.api.enums.ItineraryStatus;
import com.tripjoy.api.enums.MapProvider;
import com.tripjoy.api.exception.AppException;
import com.tripjoy.api.exception.ErrorCode;
import com.tripjoy.api.mapper.ItineraryMapper;
import com.tripjoy.api.repository.ItineraryRepository;
import com.tripjoy.api.repository.LocationRepository;
import com.tripjoy.api.repository.TripItemRepository;
import com.tripjoy.api.repository.UserRepository;
import com.tripjoy.api.service.IAiService;
import com.tripjoy.api.service.IGooglePlacesService;
import com.tripjoy.api.service.IItineraryGenerationService;
import com.tripjoy.api.service.IThemeService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItineraryGenerationService implements IItineraryGenerationService {

    private final ItineraryRepository itineraryRepository;
    private final LocationRepository locationRepository;
    private final TripItemRepository tripItemRepository;
    private final UserRepository userRepository;

    private final IAiService aiService;
    private final IGooglePlacesService googlePlacesService;
    private final IThemeService themeService;
    private final ItineraryMapper itineraryMapper;

    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    @Override
    @Transactional
    public ItineraryResponse initiateGeneration(GenerateItineraryRequest request, UUID userId) {
        log.info("Initiating itinerary generation for destination: {}", request.getDestination());

        User user = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // 1. Create Placeholder Itinerary
        Itinerary itinerary = Itinerary.builder()
                .name("Trip to " + request.getDestination())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .peopleQuantity(request.getPeopleQuantity())
                .budgetEstimate(
                        request.getBudgetEstimate() != null ? BigDecimal.valueOf(request.getBudgetEstimate()) : null)
                .status(ItineraryStatus.GENERATING)
                .user(user)
                .build();

        if (request.getThemes() != null && !request.getThemes().isEmpty()) {
            itinerary.setThemes(themeService.syncThemes(request.getThemes()));
        }

        Itinerary savedItinerary = itineraryRepository.save(itinerary);

        // Return bare minimal response with ID so client can poll
        return ItineraryResponse.builder()
                .id(savedItinerary.getId())
                .title(savedItinerary.getName())
                .status(savedItinerary.getStatus())
                .build();
    }

    @Async
    @Override
    @Transactional
    public void processGenerationAsync(UUID itineraryId, GenerateItineraryRequest request) {
        log.info("Starting background async generation for itinerary ID: {}", itineraryId);

        try {
            // 1. Map Core request to AI request
            AiTravelRequestDto aiRequest = mapToAiRequest(request);

            // 2. Call AI Service (Blocking wait in this async thread is perfectly fine)
            AiFinalItineraryDto aiResponse =
                    aiService.generateItinerary(aiRequest).block();

            if (aiResponse == null || aiResponse.getTripItems() == null) {
                log.error("AI Service returned empty or null trip items for itinerary ID: {}", itineraryId);
                updateItineraryStatus(itineraryId, ItineraryStatus.FAILED);
                return;
            }

            // 2.5 Resolve missing place_ids via Google Places Autocomplete API
            resolveMissingPlaceIds(
                    aiResponse.getTripItems(), request.getDestination(), request.getLatitude(), request.getLongitude());

            // 3. Extract all place_ids and enrich data concurrently
            Set<String> placeIds = aiResponse.getTripItems().stream()
                    .map(item -> item.getPlaceId())
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            log.info("Found {} unique place_ids from AI response. Enriching data...", placeIds.size());
            enrichAndSaveLocations(placeIds);

            // 4. Save Final Itinerary and Trip Items
            Itinerary itinerary = itineraryRepository
                    .findById(itineraryId)
                    .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

            itinerary.setName(aiResponse.getName());
            itinerary.setStatus(ItineraryStatus.DRAFT);

            List<TripItem> tripItems = aiResponse.getTripItems().stream()
                    .map(aiItem -> {
                        Location location = locationRepository
                                .findByProviderId(aiItem.getPlaceId())
                                .orElse(null);

                        return TripItem.builder()
                                .itinerary(itinerary)
                                .location(location)
                                .note(aiItem.getNote())
                                .duration(aiItem.getDuration())
                                .startTime(LocalDateTime.parse(aiItem.getStartTime(), DateTimeFormatter.ISO_DATE_TIME))
                                .build();
                    })
                    .collect(Collectors.toList());

            tripItemRepository.saveAll(tripItems);
            itineraryRepository.save(itinerary);

            log.info("Successfully completed itinerary generation for ID: {}", itineraryId);

        } catch (Exception e) {
            log.error("Failed to generate itinerary for ID {}: {}", itineraryId, e.getMessage(), e);
            updateItineraryStatus(itineraryId, ItineraryStatus.FAILED);
        }
    }

    private void updateItineraryStatus(UUID itineraryId, ItineraryStatus status) {
        itineraryRepository.findById(itineraryId).ifPresent(itinerary -> {
            itinerary.setStatus(status);
            itineraryRepository.save(itinerary);
        });
    }

    private AiTravelRequestDto mapToAiRequest(GenerateItineraryRequest req) {
        return AiTravelRequestDto.builder()
                .destinationName(req.getDestination())
                .coordinate(AiCoordinateDto.builder()
                        .latitude(req.getLatitude())
                        .longitude(req.getLongitude())
                        .build())
                .travelType(req.getThemes() != null ? List.copyOf(req.getThemes()) : List.of())
                .budget(
                        req.getBudgetEstimate() != null
                                ? req.getBudgetEstimate().longValue()
                                : 5000000L)
                .startDate(req.getStartDate().toLocalDate().toString())
                .endDate(req.getEndDate().toLocalDate().toString())
                .peopleQuantity(req.getPeopleQuantity())
                .suggestLocations(req.getSuggestLocations())
                .build();
    }

    /**
     * Queries DB for existing locations. For missing ones, fetches from Google
     * Places API concurrently.
     */
    private void enrichAndSaveLocations(Set<String> placeIds) {
        if (placeIds.isEmpty()) return;

        List<Location> existingLocations = locationRepository.findByProviderIdIn(placeIds);
        Set<String> existingPlaceIds =
                existingLocations.stream().map(Location::getProviderId).collect(Collectors.toSet());

        Set<String> missingPlaceIds =
                placeIds.stream().filter(id -> !existingPlaceIds.contains(id)).collect(Collectors.toSet());

        if (missingPlaceIds.isEmpty()) return;

        // Fetch missing places concurrently using WebClient and Reactor
        List<Location> newLocations = Flux.fromIterable(missingPlaceIds)
                .flatMap(placeId ->
                        googlePlacesService.getPlaceDetails(placeId).subscribeOn(Schedulers.boundedElastic()))
                .filter(Objects::nonNull)
                .map(this::mapGooglePlaceToLocation)
                .collectList()
                .block(); // Block since we are already in an Async thread

        if (newLocations != null && !newLocations.isEmpty()) {
            log.info("Successfully fetched {} new locations from Google Maps. Saving to DB...", newLocations.size());
            locationRepository.saveAll(newLocations);
        } else {
            log.warn("Failed to fetch any new locations from Google Maps for IDs: {}", missingPlaceIds);
        }
    }

    private Location mapGooglePlaceToLocation(GooglePlaceDetailsDto dto) {
        String rawId = dto.getId();
        String normalizedId = (rawId != null && rawId.startsWith("places/")) ? rawId.substring(7) : rawId;

        Location location = Location.builder()
                .provider(MapProvider.GOOGLE_MAPS)
                .providerId(normalizedId)
                .name(dto.getDisplayName() != null ? dto.getDisplayName().getText() : "Unknown Location")
                .fullAddress(dto.getFormattedAddress())
                .poiCategories(dto.getTypes())
                .build();

        if (dto.getLocation() != null
                && dto.getLocation().getLatitude() != null
                && dto.getLocation().getLongitude() != null) {
            location.setLatitude(dto.getLocation().getLatitude());
            location.setLongitude(dto.getLocation().getLongitude());
            Point point = geometryFactory.createPoint(new Coordinate(
                    dto.getLocation().getLongitude(), dto.getLocation().getLatitude()));
            location.setCoordinates(point);
        }

        return location;
    }

    // =========================================================================
    // MODIFY ITINERARY
    // =========================================================================

    @Override
    @Transactional
    public ItineraryResponse modifyItinerary(UUID itineraryId, AiModifyItineraryRequest request) {
        log.info(
                "Modifying itinerary ID: {} — replacing {} place(s)",
                itineraryId,
                request.getUnwantedPlaceIds().size());

        // 1. Load itinerary
        Itinerary itinerary = itineraryRepository
                .findById(itineraryId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        // 2. Load all existing TripItems
        List<TripItem> currentItems = tripItemRepository.findByItineraryId(itineraryId);

        // 3. Identify unwanted TripItems
        Set<String> unwantedPlaceIds = Set.copyOf(request.getUnwantedPlaceIds());

        List<TripItem> unwantedItems = currentItems.stream()
                .filter(item -> item.getLocation() != null
                        && unwantedPlaceIds.contains(item.getLocation().getProviderId()))
                .collect(Collectors.toList());

        if (unwantedItems.isEmpty()) {
            log.warn("No matching TripItems found for place IDs: {}", unwantedPlaceIds);
            throw new AppException(
                    ErrorCode.INVALID_REQUEST,
                    "No matching trip items found for the provided place IDs. Please provide valid Google place_ids.");
        }

        // 4. Build AiFinalItineraryDto từ itinerary hiện tại (để gửi sang AI)
        List<AiTripItemDto> aiAllItems = currentItems.stream()
                .map(item -> AiTripItemDto.builder()
                        .startTime(
                                item.getStartTime() != null
                                        ? item.getStartTime().toString()
                                        : null)
                        .duration(item.getDuration())
                        .note(item.getNote())
                        .locationName(
                                item.getLocation() != null ? item.getLocation().getName() : null)
                        .placeId(item.getLocation() != null ? item.getLocation().getProviderId() : null)
                        .build())
                .collect(Collectors.toList());

        AiFinalItineraryDto aiItineraryDto = AiFinalItineraryDto.builder()
                .name(itinerary.getName())
                .startDate(
                        itinerary.getStartDate() != null
                                ? itinerary.getStartDate().toLocalDate().toString()
                                : null)
                .endDate(
                        itinerary.getEndDate() != null
                                ? itinerary.getEndDate().toLocalDate().toString()
                                : null)
                .peopleQuantity(itinerary.getPeopleQuantity())
                .budgetEstimate(
                        itinerary.getBudgetEstimate() != null
                                ? itinerary.getBudgetEstimate().longValue()
                                : null)
                .themes(itinerary.getThemes().stream().map(t -> t.getName()).collect(Collectors.toList()))
                .destination(
                        itinerary.getDestination() != null
                                ? itinerary.getDestination().getName()
                                : itinerary
                                        .getName()
                                        .replaceFirst("(?i)^trip to ", "")
                                        .trim())
                .tripItems(aiAllItems)
                .build();

        // 5. Build unwanted locations as AiTripItemDto (full objects)
        List<AiTripItemDto> aiUnwantedItems = unwantedItems.stream()
                .map(item -> AiTripItemDto.builder()
                        .startTime(
                                item.getStartTime() != null
                                        ? item.getStartTime().toString()
                                        : null)
                        .duration(item.getDuration())
                        .note(item.getNote())
                        .locationName(
                                item.getLocation() != null ? item.getLocation().getName() : null)
                        .placeId(item.getLocation() != null ? item.getLocation().getProviderId() : null)
                        .build())
                .collect(Collectors.toList());

        // 6. Resolve coordinate từ TripItem hiện có (dùng cho cả AI request lẫn resolve sau)
        TripItem referenceItem = currentItems.stream()
                .filter(item -> item.getLocation() != null && item.getLocation().getLatitude() != null)
                .findFirst()
                .orElse(unwantedItems.get(0));

        Double lat = referenceItem.getLocation() != null
                ? referenceItem.getLocation().getLatitude()
                : 0.0;
        Double lng = referenceItem.getLocation() != null
                ? referenceItem.getLocation().getLongitude()
                : 0.0;

        // 7. Gọi AI Service — coordinate required by Python modify_itinerary()
        AiModifyItineraryRequestDto aiRequest = AiModifyItineraryRequestDto.builder()
                .itineraryData(aiItineraryDto)
                .unwantedLocations(aiUnwantedItems)
                .coordinate(
                        AiCoordinateDto.builder().latitude(lat).longitude(lng).build())
                .build();

        AiFinalItineraryDto aiResponse = aiService.modifyItinerary(aiRequest).block();

        if (aiResponse == null || aiResponse.getTripItems() == null) {
            log.error("AI Service returned empty response for modify-itinerary on ID: {}", itineraryId);
            throw new AppException(ErrorCode.AI_SERVICE_UNAVAILABLE);
        }

        // 7.5. Resolve missing place_ids
        resolveMissingPlaceIds(aiResponse.getTripItems(), itinerary.getName(), lat, lng);

        // 8. Enrich + save new locations từ AI response
        Set<String> newPlaceIds = aiResponse.getTripItems().stream()
                .map(AiTripItemDto::getPlaceId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        enrichAndSaveLocations(newPlaceIds);

        // 9. Xác định các TripItem thay thế (những item mới từ AI chưa có trong kept set)
        Set<String> keptPlaceIds = currentItems.stream()
                .filter(item -> !unwantedItems.contains(item))
                .filter(item -> item.getLocation() != null)
                .map(item -> item.getLocation().getProviderId())
                .collect(Collectors.toSet());

        List<TripItem> replacementItems = aiResponse.getTripItems().stream()
                .filter(aiItem -> aiItem.getPlaceId() != null && !keptPlaceIds.contains(aiItem.getPlaceId()))
                .map(aiItem -> {
                    Location location = locationRepository
                            .findByProviderId(aiItem.getPlaceId())
                            .orElse(null);
                    return TripItem.builder()
                            .itinerary(itinerary)
                            .location(location)
                            .note(aiItem.getNote())
                            .duration(aiItem.getDuration())
                            .startTime(
                                    aiItem.getStartTime() != null
                                            ? LocalDateTime.parse(
                                                    aiItem.getStartTime(), DateTimeFormatter.ISO_DATE_TIME)
                                            : null)
                            .build();
                })
                .collect(Collectors.toList());

        // 10. Update itinerary in-memory then persist
        itinerary.getTripItems().removeAll(unwantedItems);
        log.info("Removed {} unwanted trip item(s) from itinerary ID: {}", unwantedItems.size(), itineraryId);

        replacementItems.forEach(item -> {
            item.setItinerary(itinerary);
            itinerary.getTripItems().add(item);
        });
        log.info("Added {} replacement trip item(s) for itinerary ID: {}", replacementItems.size(), itineraryId);

        itineraryRepository.save(itinerary);
        return itineraryMapper.toItineraryResponse(itinerary);
    }

    // =========================================================================
    // SUGGEST LOCATION (single replacement)
    // =========================================================================

    @Override
    @Transactional(readOnly = true)
    public List<TripItemResponse> suggestLocation(UUID itineraryId, AiSuggestLocationRequest request) {
        log.info(
                "Suggesting location replacement for itinerary ID: {}, unwanted place: {}",
                itineraryId,
                request.getUnwantedPlaceId());

        // 1. Load itinerary + trip items
        Itinerary itinerary = itineraryRepository
                .findById(itineraryId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        List<TripItem> currentItems = tripItemRepository.findByItineraryId(itineraryId);

        // 2. Find the unwanted TripItem
        TripItem unwantedItem = currentItems.stream()
                .filter(item -> item.getLocation() != null
                        && request.getUnwantedPlaceId()
                                .equals(item.getLocation().getProviderId()))
                .findFirst()
                .orElseThrow(() -> new AppException(
                        ErrorCode.INVALID_REQUEST,
                        "No trip item found with place_id: " + request.getUnwantedPlaceId()));

        AiTripItemDto unwantedDto = AiTripItemDto.builder()
                .startTime(
                        unwantedItem.getStartTime() != null
                                ? unwantedItem.getStartTime().toString()
                                : null)
                .duration(unwantedItem.getDuration())
                .note(unwantedItem.getNote())
                .locationName(unwantedItem.getLocation().getName())
                .placeId(unwantedItem.getLocation().getProviderId())
                .build();

        // 3. Build full itinerary DTO
        List<AiTripItemDto> aiAllItems = currentItems.stream()
                .map(item -> AiTripItemDto.builder()
                        .startTime(
                                item.getStartTime() != null
                                        ? item.getStartTime().toString()
                                        : null)
                        .duration(item.getDuration())
                        .note(item.getNote())
                        .locationName(
                                item.getLocation() != null ? item.getLocation().getName() : null)
                        .placeId(item.getLocation() != null ? item.getLocation().getProviderId() : null)
                        .build())
                .collect(Collectors.toList());

        AiFinalItineraryDto aiItineraryDto = AiFinalItineraryDto.builder()
                .name(itinerary.getName())
                .startDate(
                        itinerary.getStartDate() != null
                                ? itinerary.getStartDate().toLocalDate().toString()
                                : null)
                .endDate(
                        itinerary.getEndDate() != null
                                ? itinerary.getEndDate().toLocalDate().toString()
                                : null)
                .peopleQuantity(itinerary.getPeopleQuantity())
                .budgetEstimate(
                        itinerary.getBudgetEstimate() != null
                                ? itinerary.getBudgetEstimate().longValue()
                                : null)
                .themes(itinerary.getThemes().stream().map(t -> t.getName()).collect(Collectors.toList()))
                .destination(
                        itinerary.getDestination() != null
                                ? itinerary.getDestination().getName()
                                : itinerary
                                        .getName()
                                        .replaceFirst("(?i)^trip to ", "")
                                        .trim())
                .tripItems(aiAllItems)
                .build();

        // 4. Resolve coordinate từ request hoặc lấy từ TripItem hiện có
        Double lat = request.getLatitude();
        Double lng = request.getLongitude();
        if (lat == null || lng == null) {
            TripItem ref = currentItems.stream()
                    .filter(item ->
                            item.getLocation() != null && item.getLocation().getLatitude() != null)
                    .findFirst()
                    .orElse(null);
            if (ref != null) {
                lat = ref.getLocation().getLatitude();
                lng = ref.getLocation().getLongitude();
            }
        }

        // 5. Call AI Service
        AiSuggestLocationRequestDto aiRequest = AiSuggestLocationRequestDto.builder()
                .itineraryData(aiItineraryDto)
                .unwantedLocation(unwantedDto)
                .coordinate(AiCoordinateDto.builder()
                        .latitude(lat != null ? lat : 0.0)
                        .longitude(lng != null ? lng : 0.0)
                        .build())
                .build();

        List<AiTripItemDto> aiSuggestions = aiService.suggestLocation(aiRequest).block();

        if (aiSuggestions == null || aiSuggestions.isEmpty()) {
            log.warn("AI Service returned no suggestions for place: {}", request.getUnwantedPlaceId());
            return List.of();
        }

        // 6. Map AiTripItemDto → TripItemResponse (lightweight, không lưu DB)
        return aiSuggestions.stream()
                .map(ai -> TripItemResponse.builder()
                        .note(ai.getNote())
                        .duration(ai.getDuration())
                        .startTime(
                                ai.getStartTime() != null
                                        ? LocalDateTime.parse(ai.getStartTime(), DateTimeFormatter.ISO_DATE_TIME)
                                        : null)
                        .location(com.tripjoy.api.dto.response.location.LocationResponse.builder()
                                .name(ai.getLocationName())
                                .providerId(ai.getPlaceId())
                                .build())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Finds items missing a place_id and queries the Google Places Autocomplete API
     * using the locationName to attempt auto-resolution before inserting into DB.
     */
    private void resolveMissingPlaceIds(List<AiTripItemDto> items, String cityBias, Double lat, Double lng) {
        log.info("Resolving missing place_ids for locations via Google Autocomplete API...");
        Flux.fromIterable(items)
                .filter(item -> (item.getPlaceId() == null || item.getPlaceId().isBlank())
                        && item.getLocationName() != null
                        && !item.getLocationName().isBlank())
                .flatMap(item -> {
                    log.debug("Auto-resolving location: {}", item.getLocationName());
                    return googlePlacesService
                            .autocomplete(item.getLocationName(), cityBias, lat, lng)
                            .map(resp -> {
                                if (resp != null
                                        && resp.getSuggestions() != null
                                        && !resp.getSuggestions().isEmpty()) {
                                    String resolvedPlaceId = resp.getSuggestions()
                                            .get(0)
                                            .getPlacePrediction()
                                            .getPlaceId();
                                    item.setPlaceId(resolvedPlaceId);
                                    log.info("Resolved '{}' -> place_id: {}", item.getLocationName(), resolvedPlaceId);
                                } else {
                                    log.warn("Could not resolve '{}' on Google Maps.", item.getLocationName());
                                }
                                return item;
                            })
                            .subscribeOn(Schedulers.boundedElastic());
                })
                .collectList()
                .block(); // Wait for all resolving to finish
    }

    @Override
    @Transactional(readOnly = true)
    public AiFinalItineraryDto getLatestAiItineraryByGroupId(UUID groupId) {
        List<Itinerary> itineraries = itineraryRepository.findByGroupIdAndNotDeleted(groupId);
        if (itineraries == null || itineraries.isEmpty()) {
            return null;
        }

        // Sort by updated_at desc to get the latest
        Itinerary latest = itineraries.stream()
                .max((i1, i2) -> {
                    LocalDateTime t1 = i1.getUpdatedAt() != null ? i1.getUpdatedAt() : i1.getCreatedAt();
                    LocalDateTime t2 = i2.getUpdatedAt() != null ? i2.getUpdatedAt() : i2.getCreatedAt();
                    if (t1 == null) return -1;
                    if (t2 == null) return 1;
                    return t1.compareTo(t2);
                })
                .orElse(null);

        if (latest == null) return null;

        List<TripItem> items = tripItemRepository.findByItineraryId(latest.getId());

        return AiFinalItineraryDto.builder()
                .name(latest.getName())
                .startDate(
                        latest.getStartDate() != null
                                ? latest.getStartDate().toLocalDate().toString()
                                : null)
                .endDate(
                        latest.getEndDate() != null
                                ? latest.getEndDate().toLocalDate().toString()
                                : null)
                .peopleQuantity(latest.getPeopleQuantity())
                .budgetEstimate(
                        latest.getBudgetEstimate() != null
                                ? latest.getBudgetEstimate().longValue()
                                : null)
                .themes(
                        latest.getThemes() != null
                                ? latest.getThemes().stream()
                                        .map(Theme::getName)
                                        .collect(Collectors.toList())
                                : null)
                .destination(
                        latest.getDestination() != null
                                ? latest.getDestination().getName()
                                : null)
                .tripItems(items.stream()
                        .map(item -> AiTripItemDto.builder()
                                .startTime(
                                        item.getStartTime() != null
                                                ? item.getStartTime().toString()
                                                : null)
                                .duration(item.getDuration())
                                .note(item.getNote())
                                .locationName(
                                        item.getLocation() != null
                                                ? item.getLocation().getName()
                                                : null)
                                .placeId(
                                        item.getLocation() != null
                                                ? item.getLocation().getProviderId()
                                                : null)
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }
}
