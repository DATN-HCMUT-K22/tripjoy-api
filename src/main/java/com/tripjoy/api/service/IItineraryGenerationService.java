package com.tripjoy.api.service;

import com.tripjoy.api.dto.request.AiModifyItineraryRequest;
import com.tripjoy.api.dto.request.GenerateItineraryRequest;
import com.tripjoy.api.dto.response.ItineraryResponse;

import java.util.UUID;

public interface IItineraryGenerationService {
    
    /**
     * Entry point for itinerary generation.
     * Saves Itinerary with GENERATING status and quickly returns response.
     */
    ItineraryResponse initiateGeneration(GenerateItineraryRequest request, UUID userId);
    
    /**
     * Background async job that actually calls the AI and Map providers.
     */
    void processGenerationAsync(UUID itineraryId, GenerateItineraryRequest request);

    /**
     * Synchronously modifies an existing itinerary by replacing unwanted trip-item locations
     * with new ones chosen by the AI. Blocks for up to the configured AI timeout.
     *
     * @param itineraryId     ID of the itinerary to modify
     * @param request         Contains list of place_ids to replace
     * @return Updated ItineraryResponse
     */
    ItineraryResponse modifyItinerary(UUID itineraryId, AiModifyItineraryRequest request);
}

