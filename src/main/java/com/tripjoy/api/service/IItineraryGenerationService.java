package com.tripjoy.api.service;

import java.util.List;
import java.util.UUID;

import com.tripjoy.api.dto.ai.AiFinalItineraryDto;
import com.tripjoy.api.dto.request.AiModifyItineraryRequest;
import com.tripjoy.api.dto.request.AiSuggestLocationRequest;
import com.tripjoy.api.dto.request.GenerateItineraryRequest;
import com.tripjoy.api.dto.response.ItineraryResponse;
import com.tripjoy.api.dto.response.TripItemResponse;

public interface IItineraryGenerationService {

    AiFinalItineraryDto getLatestAiItineraryByGroupId(UUID groupId);

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
     * with new ones chosen by the AI.
     *
     * @param itineraryId ID of the itinerary to modify
     * @param request     Contains list of place_ids to replace
     * @return Updated ItineraryResponse
     */
    ItineraryResponse modifyItinerary(UUID itineraryId, AiModifyItineraryRequest request);

    /**
     * Asks AI to suggest alternative locations for a single unwanted TripItem.
     * Does NOT persist changes — returns candidate TripItemResponses for user review.
     *
     * @param itineraryId ID of the itinerary (used to build context for AI)
     * @param request     Contains place_id of unwanted item + optional coordinates
     * @return List of AI-suggested TripItemResponse candidates
     */
    List<TripItemResponse> suggestLocation(UUID itineraryId, AiSuggestLocationRequest request);
}
