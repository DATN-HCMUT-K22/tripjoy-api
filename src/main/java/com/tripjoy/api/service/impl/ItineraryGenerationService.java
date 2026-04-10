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
import com.tripjoy.api.dto.ai.AiTravelRequestDto;
import com.tripjoy.api.dto.ai.AiTripItemDto;
import com.tripjoy.api.dto.ai.GooglePlaceDetailsDto;
import com.tripjoy.api.dto.request.AiModifyItineraryRequest;
import com.tripjoy.api.dto.request.GenerateItineraryRequest;
import com.tripjoy.api.dto.response.ItineraryResponse;
import com.tripjoy.api.entity.Itinerary;
import com.tripjoy.api.entity.Location;
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
import com.tripjoy.api.service.IThemeService;
import com.tripjoy.api.service.IItineraryGenerationService;

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

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

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
            AiFinalItineraryDto aiResponse = aiService.generateItinerary(aiRequest).block();

            if (aiResponse == null || aiResponse.getTripItems() == null) {
                log.error("AI Service returned empty or null trip items for itinerary ID: {}", itineraryId);
                updateItineraryStatus(itineraryId, ItineraryStatus.FAILED);
                return;
            }

            // 3. Extract all place_ids and enrich data concurrently
            Set<String> placeIds = aiResponse.getTripItems().stream()
                    .map(item -> item.getPlaceId())
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            log.info("Found {} unique place_ids from AI response. Enriching data...", placeIds.size());
            enrichAndSaveLocations(placeIds);

            // 4. Save Final Itinerary and Trip Items
            Itinerary itinerary = itineraryRepository.findById(itineraryId)
                    .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

            itinerary.setName(aiResponse.getName());
            itinerary.setStatus(ItineraryStatus.DRAFT);

            List<TripItem> tripItems = aiResponse.getTripItems().stream().map(aiItem -> {
                Location location = locationRepository.findByProviderId(aiItem.getPlaceId()).orElse(null);

                return TripItem.builder()
                        .itinerary(itinerary)
                        .location(location)
                        .note(aiItem.getNote())
                        .duration(aiItem.getDuration())
                        .startTime(LocalDateTime.parse(aiItem.getStartTime(), DateTimeFormatter.ISO_DATE_TIME)) // Assuming
                                                                                                                // ISO
                                                                                                                // format
                        .build();
            }).collect(Collectors.toList());

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
                .travelType(List.copyOf(req.getThemes()))
                .budget(req.getBudgetEstimate() != null ? String.valueOf(req.getBudgetEstimate()) : "VND 5000000")
                .startDate(req.getStartDate().toLocalDate().toString())
                .endDate(req.getEndDate().toLocalDate().toString())
                .peopleQuantity(req.getPeopleQuantity())
                .build();
    }

    /**
     * Queries DB for existing locations. For missing ones, fetches from Google
     * Places API concurrently.
     */
    private void enrichAndSaveLocations(Set<String> placeIds) {
        if (placeIds.isEmpty())
            return;

        List<Location> existingLocations = locationRepository.findByProviderIdIn(placeIds);
        Set<String> existingPlaceIds = existingLocations.stream()
                .map(Location::getProviderId)
                .collect(Collectors.toSet());

        Set<String> missingPlaceIds = placeIds.stream()
                .filter(id -> !existingPlaceIds.contains(id))
                .collect(Collectors.toSet());

        if (missingPlaceIds.isEmpty())
            return;

        // Fetch missing places concurrently using WebClient and Reactor
        List<Location> newLocations = Flux.fromIterable(missingPlaceIds)
                .flatMap(placeId -> googlePlacesService.getPlaceDetails(placeId)
                        .subscribeOn(Schedulers.boundedElastic()))
                .filter(Objects::nonNull)
                .map(this::mapGooglePlaceToLocation)
                .collectList()
                .block(); // Block since we are already in an Async thread

        if (newLocations != null && !newLocations.isEmpty()) {
            locationRepository.saveAll(newLocations);
        }
    }

    private Location mapGooglePlaceToLocation(GooglePlaceDetailsDto dto) {
        Location location = Location.builder()
                .provider(MapProvider.GOOGLE_MAPS)
                .providerId(dto.getId())
                .name(dto.getDisplayName() != null ? dto.getDisplayName().getText() : "Unknown Location")
                .fullAddress(dto.getFormattedAddress())
                .poiCategories(dto.getTypes())
                .build();

        if (dto.getLocation() != null && dto.getLocation().getLatitude() != null
                && dto.getLocation().getLongitude() != null) {
            location.setLatitude(dto.getLocation().getLatitude());
            location.setLongitude(dto.getLocation().getLongitude());
            Point point = geometryFactory
                    .createPoint(new Coordinate(dto.getLocation().getLongitude(), dto.getLocation().getLatitude()));
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
        log.info("Modifying itinerary ID: {} — replacing {} place(s)",
                itineraryId, request.getUnwantedPlaceIds().size());

        // 1. Load itinerary
        Itinerary itinerary = itineraryRepository.findById(itineraryId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        // 2. Load all existing TripItems  
        List<TripItem> currentItems = tripItemRepository.findByItineraryId(itineraryId);

        // 3. Identify unwanted TripItems (những item có location.providerId nằm trong danh sách client gửi)
        Set<String> unwantedPlaceIds = Set.copyOf(request.getUnwantedPlaceIds());

        List<TripItem> unwantedItems = currentItems.stream()
                .filter(item -> item.getLocation() != null
                        && unwantedPlaceIds.contains(item.getLocation().getProviderId()))
                .collect(Collectors.toList());

        if (unwantedItems.isEmpty()) {
            log.warn("No matching TripItems found for place IDs: {}", unwantedPlaceIds);
            return itineraryMapper.toItineraryResponse(itinerary);
        }

        // 4. Lấy tọa độ destination từ TripItem đầu tiên còn lại (hoặc từ unwanted nếu không có)
        TripItem referencItem = currentItems.stream()
                .filter(item -> item.getLocation() != null && item.getLocation().getLatitude() != null)
                .findFirst()
                .orElse(unwantedItems.get(0));

        Double lat = referencItem.getLocation() != null ? referencItem.getLocation().getLatitude() : 0.0;
        Double lng = referencItem.getLocation() != null ? referencItem.getLocation().getLongitude() : 0.0;

        // 5. Build AiFinalItineraryDto từ itinerary hiện tại (để gửi sang AI)
        List<AiTripItemDto> aiAllItems = currentItems.stream()
                .map(item -> AiTripItemDto.builder()
                        .startTime(item.getStartTime() != null ? item.getStartTime().toString() : null)
                        .duration(item.getDuration())
                        .note(item.getNote())
                        .locationName(item.getLocation() != null ? item.getLocation().getName() : null)
                        .placeId(item.getLocation() != null ? item.getLocation().getProviderId() : null)
                        .build())
                .collect(Collectors.toList());

        AiFinalItineraryDto aiItineraryDto = AiFinalItineraryDto.builder()
                .name(itinerary.getName())
                .startDate(itinerary.getStartDate() != null ? itinerary.getStartDate().toLocalDate().toString() : null)
                .endDate(itinerary.getEndDate() != null ? itinerary.getEndDate().toLocalDate().toString() : null)
                .peopleQuantity(itinerary.getPeopleQuantity())
                .budgetEstimate(itinerary.getBudgetEstimate() != null ? itinerary.getBudgetEstimate().toString() : null)
                .themes(itinerary.getThemes().stream().map(t -> t.getName()).collect(Collectors.toList()))
                .destination(itinerary.getName())
                .tripItems(aiAllItems)
                .build();

        // 6. Build unwanted locations dưới dạng AiTripItemDto
        List<AiTripItemDto> aiUnwantedItems = unwantedItems.stream()
                .map(item -> AiTripItemDto.builder()
                        .startTime(item.getStartTime() != null ? item.getStartTime().toString() : null)
                        .duration(item.getDuration())
                        .note(item.getNote())
                        .locationName(item.getLocation() != null ? item.getLocation().getName() : null)
                        .placeId(item.getLocation() != null ? item.getLocation().getProviderId() : null)
                        .build())
                .collect(Collectors.toList());

        // 7. Gọi AI Service
        AiModifyItineraryRequestDto aiRequest = AiModifyItineraryRequestDto.builder()
                .itineraryData(aiItineraryDto)
                .unwantedLocations(aiUnwantedItems)
                .coordinate(AiCoordinateDto.builder().latitude(lat).longitude(lng).build())
                .build();

        AiFinalItineraryDto aiResponse = aiService.modifyItinerary(aiRequest).block();

        if (aiResponse == null || aiResponse.getTripItems() == null) {
            log.error("AI Service returned empty response for modify-itinerary on ID: {}", itineraryId);
            throw new AppException(ErrorCode.AI_SERVICE_UNAVAILABLE);
        }

        // 8. Enrich + save new locations từ AI response
        Set<String> newPlaceIds = aiResponse.getTripItems().stream()
                .map(AiTripItemDto::getPlaceId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        enrichAndSaveLocations(newPlaceIds);

        // 9. Tìm các TripItem mới do AI trả về (những item có place_id không trùng với currentItems)
        //    Chiến lược: AI trả về toàn bộ itinerary đã modified  
        //    → chỉ những placeId mới (không nằm trong unwantedPlaceIds cũ) được giữ lại từ currentItems
        //    → những placeId trong unwantedPlaceIds cũ bị xóa + thay thế bằng item từ aiResponse

        // Xóa unwanted trip items
        tripItemRepository.deleteAll(unwantedItems);
        log.info("Deleted {} unwanted trip item(s) from itinerary ID: {}", unwantedItems.size(), itineraryId);

        // Lọc ra các TripItem MỚI từ aiResponse (những item có placeId không trùng với keep items)
        Set<String> keptPlaceIds = currentItems.stream()
                .filter(item -> !unwantedItems.contains(item))
                .filter(item -> item.getLocation() != null)
                .map(item -> item.getLocation().getProviderId())
                .collect(Collectors.toSet());

        List<TripItem> replacementItems = aiResponse.getTripItems().stream()
                .filter(aiItem -> aiItem.getPlaceId() != null && !keptPlaceIds.contains(aiItem.getPlaceId()))
                .map(aiItem -> {
                    Location location = locationRepository.findByProviderId(aiItem.getPlaceId()).orElse(null);
                    return TripItem.builder()
                            .itinerary(itinerary)
                            .location(location)
                            .note(aiItem.getNote())
                            .duration(aiItem.getDuration())
                            .startTime(aiItem.getStartTime() != null
                                    ? LocalDateTime.parse(aiItem.getStartTime(), DateTimeFormatter.ISO_DATE_TIME)
                                    : null)
                            .build();
                })
                .collect(Collectors.toList());

        tripItemRepository.saveAll(replacementItems);
        log.info("Saved {} replacement trip item(s) for itinerary ID: {}", replacementItems.size(), itineraryId);

        // 10. Reload itinerary để trả về response cập nhật
        Itinerary updatedItinerary = itineraryRepository.findById(itineraryId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        return itineraryMapper.toItineraryResponse(updatedItinerary);
    }
}
