package com.tripjoy.api.service;

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
}
