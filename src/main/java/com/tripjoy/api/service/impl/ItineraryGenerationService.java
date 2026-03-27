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
import com.tripjoy.api.dto.ai.AiTravelRequestDto;
import com.tripjoy.api.dto.ai.GooglePlaceDetailsDto;
import com.tripjoy.api.dto.request.GenerateItineraryRequest;
import com.tripjoy.api.dto.response.ItineraryResponse;
import com.tripjoy.api.entity.Itinerary;
import com.tripjoy.api.entity.ItineraryTheme;
import com.tripjoy.api.entity.Location;
import com.tripjoy.api.entity.TripItem;
import com.tripjoy.api.entity.User;
import com.tripjoy.api.enums.ItineraryStatus;
import com.tripjoy.api.enums.MapProvider;
import com.tripjoy.api.exception.AppException;
import com.tripjoy.api.exception.ErrorCode;
import com.tripjoy.api.repository.ItineraryRepository;
import com.tripjoy.api.repository.LocationRepository;
import com.tripjoy.api.repository.TripItemRepository;
import com.tripjoy.api.repository.UserRepository;
import com.tripjoy.api.service.IAiService;
import com.tripjoy.api.service.IGooglePlacesService;
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
            Set<ItineraryTheme> themes = request.getThemes().stream()
                    .map(t -> ItineraryTheme.builder()
                            .theme(t)
                            .itinerary(itinerary)
                            .build())
                    .collect(Collectors.toSet());
            itinerary.setItineraryThemes(themes);
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
}
