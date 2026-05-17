package com.tripjoy.api.service.impl;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tripjoy.api.dto.ai.AiGenerateNotebookRequestDto;
import com.tripjoy.api.dto.ai.AiNotebookResponseDto;
import com.tripjoy.api.dto.ai.AiTripItemDto;
import com.tripjoy.api.dto.response.TravelNotebookResponse;
import com.tripjoy.api.entity.Itinerary;
import com.tripjoy.api.entity.TravelNotebook;
import com.tripjoy.api.exception.AppException;
import com.tripjoy.api.exception.ErrorCode;
import com.tripjoy.api.mapper.TravelNotebookMapper;
import com.tripjoy.api.repository.ItineraryRepository;
import com.tripjoy.api.repository.TravelNotebookRepository;
import com.tripjoy.api.repository.TripItemRepository;
import com.tripjoy.api.service.IAiService;
import com.tripjoy.api.service.ITravelNotebookService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
public class TravelNotebookService implements ITravelNotebookService {

    ItineraryRepository itineraryRepository;
    TravelNotebookRepository travelNotebookRepository;
    TripItemRepository tripItemRepository;
    IAiService aiService;
    TravelNotebookMapper travelNotebookMapper;

    // =========================================================================
    // AI GENERATE
    // =========================================================================

    @Override
    public TravelNotebookResponse generateByItinerary(UUID itineraryId) {
        log.info("Generating travel notebook for itinerary ID: {}", itineraryId);

        // 1. Load itinerary
        Itinerary itinerary = itineraryRepository
                .findById(itineraryId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        // 2. Build AiGenerateNotebookRequestDto — khớp FinalItinerary Python model
        List<AiTripItemDto> aiTripItems = tripItemRepository.findByItineraryId(itineraryId).stream()
                .map(item -> AiTripItemDto.builder()
                        .startTime(item.getStartTime() != null ? item.getStartTime().toString() : null)
                        .duration(item.getDuration() != null ? item.getDuration() : 60)
                        .note(item.getNote() != null ? item.getNote() : "")
                        .locationName(item.getLocation() != null ? item.getLocation().getName() : "Unknown Location")
                        .placeId(item.getLocation() != null ? item.getLocation().getProviderId() : null)
                        .build())
                .collect(Collectors.toList());

        // Robust destination extraction
        String destinationName = "Unknown Destination";
        if (itinerary.getDestination() != null) {
            destinationName = itinerary.getDestination().getName();
        } else if (itinerary.getName() != null) {
            destinationName = itinerary.getName().replaceFirst("(?i)^Trip to\\s+", "").trim();
        }

        // Clean up: remove " Adventure X", etc.
        destinationName = destinationName
                .replaceFirst("(?i)\\s+Adventure\\s+\\d+$", "")
                .replaceFirst("(?i)\\s+Adventure$", "")
                .trim();

        AiGenerateNotebookRequestDto aiRequest = AiGenerateNotebookRequestDto.builder()
                .name(itinerary.getName() != null ? itinerary.getName() : "Untitled Trip")
                .startDate(itinerary.getStartDate() != null ? itinerary.getStartDate().toLocalDate().toString() : null)
                .endDate(itinerary.getEndDate() != null ? itinerary.getEndDate().toLocalDate().toString() : null)
                .peopleQuantity(itinerary.getPeopleQuantity() != null ? itinerary.getPeopleQuantity() : 1)
                .budgetEstimate(itinerary.getBudgetEstimate() != null ? itinerary.getBudgetEstimate().longValue() : 0L)
                .themes(itinerary.getThemes() != null 
                    ? itinerary.getThemes().stream().map(t -> t.getName()).collect(Collectors.toList()) 
                    : List.of())
                .destination(destinationName)
                .tripItems(aiTripItems)
                .build();

        // 3. Gọi AI Service — blocking vì đây là synchronous request từ client
        AiNotebookResponseDto aiResponse;
        try {
            aiResponse = aiService.generateNotebook(aiRequest).block();
        } catch (Exception e) {
            log.error("AI Service notebook generation failed: {}", e.getMessage());
            throw new AppException(ErrorCode.AI_SERVICE_UNAVAILABLE, "AI Service failed to generate notebook: " + e.getMessage());
        }

        if (aiResponse == null) {
            log.error("AI Service returned null response for notebook generation, itinerary ID: {}", itineraryId);
            throw new AppException(ErrorCode.AI_SERVICE_UNAVAILABLE, "AI Service returned no content for the notebook");
        }

        // 4. Upsert: nếu notebook đã tồn tại thì update, nếu chưa thì tạo mới
        TravelNotebook notebook = travelNotebookRepository
                .findByItineraryId(itineraryId)
                .orElse(TravelNotebook.builder().itinerary(itinerary).build());

        notebook.setName(
                aiResponse.getName() != null
                        ? aiResponse.getName()
                        : "Travel Notebook - " + aiRequest.getDestination());
        notebook.setFood(aiResponse.getFood());
        notebook.setClimate(aiResponse.getClimate());
        notebook.setCulture(aiResponse.getCulture());
        notebook.setEmergencyContacts(aiResponse.getEmergencyContacts());

        TravelNotebook saved = travelNotebookRepository.save(notebook);
        log.info("Travel notebook saved/updated for itinerary ID: {}", itineraryId);

        return travelNotebookMapper.toResponse(saved);
    }

    // =========================================================================
    // QUERIES
    // =========================================================================

    @Override
    @Transactional(readOnly = true)
    public TravelNotebookResponse getByItineraryId(UUID itineraryId) {
        TravelNotebook notebook = travelNotebookRepository
                .findByItineraryId(itineraryId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
        return travelNotebookMapper.toResponse(notebook);
    }

    @Override
    @Transactional(readOnly = true)
    public TravelNotebookResponse getById(UUID notebookId) {
        TravelNotebook notebook = travelNotebookRepository
                .findById(notebookId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
        return travelNotebookMapper.toResponse(notebook);
    }
}
