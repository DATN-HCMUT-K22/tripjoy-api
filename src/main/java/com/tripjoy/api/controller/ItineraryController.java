package com.tripjoy.api.controller;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;

import com.tripjoy.api.constant.Endpoint;
import com.tripjoy.api.dto.request.AiModifyItineraryRequest;
import com.tripjoy.api.dto.request.GenerateItineraryRequest;
import com.tripjoy.api.dto.request.ItineraryRequest;
import com.tripjoy.api.dto.request.TripItemRequest;
import com.tripjoy.api.dto.response.ApiResponse;
import com.tripjoy.api.dto.response.ItineraryResponse;
import com.tripjoy.api.dto.response.TripItemResponse;
import com.tripjoy.api.service.IItineraryService;
import com.tripjoy.api.service.IItineraryGenerationService;
import com.tripjoy.api.utils.SecurityUtils;
import org.springframework.http.ResponseEntity;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping(Endpoint.Itinerary.BASE)
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Itinerary", description = "Endpoints for managing itineraries and trip items")
public class ItineraryController {

    IItineraryService itineraryService;
    IItineraryGenerationService itineraryGenerationService;

    @Operation(summary = "Generate AI Itinerary (Async)")
    @PostMapping(Endpoint.Itinerary.AI_GENERATE)
    public ResponseEntity<ApiResponse<ItineraryResponse>> generateItinerary(
            @Valid @RequestBody GenerateItineraryRequest request) {

        UUID userId = SecurityUtils.getCurrentUserId();

        // 1. Save placeholder generating state
        ItineraryResponse response = itineraryGenerationService.initiateGeneration(request, userId);

        // 2. Trigger asynchronous processing
        itineraryGenerationService.processGenerationAsync(response.getId(), request);

        // 3. Return 202 Accepted
        return ResponseEntity.accepted().body(
                ApiResponse.<ItineraryResponse>builder()
                        .message("Itinerary generation has started and will be available shortly")
                        .data(response)
                        .build());
    }

    @Operation(summary = "Create a new itinerary")
    @PostMapping
    public ApiResponse<ItineraryResponse> createItinerary(@Valid @RequestBody ItineraryRequest request) {
        return ApiResponse.<ItineraryResponse>builder()
                .data(itineraryService.createItinerary(request))
                .build();
    }

    @Operation(summary = "Get a single itinerary by ID")
    @GetMapping(Endpoint.Itinerary.ID)
    public ApiResponse<ItineraryResponse> getItinerary(@PathVariable UUID id) {
        return ApiResponse.<ItineraryResponse>builder()
                .data(itineraryService.getItineraryById(id))
                .build();
    }

    @GetMapping(Endpoint.Itinerary.ME)
    public ApiResponse<List<ItineraryResponse>> getMyItineraries() {
        return ApiResponse.<List<ItineraryResponse>>builder()
                .data(itineraryService.getMyItineraries())
                .build();
    }

    @GetMapping(Endpoint.Itinerary.FAVORITES)
    public ApiResponse<List<ItineraryResponse>> getMyFavoriteItineraries() {
        return ApiResponse.<List<ItineraryResponse>>builder()
                .data(itineraryService.getMyFavoriteItineraries())
                .build();
    }

    @Operation(summary = "Update an itinerary")
    @PutMapping(Endpoint.Itinerary.ID)
    public ApiResponse<ItineraryResponse> updateItinerary(
            @PathVariable UUID itineraryId, @Valid @RequestBody ItineraryRequest request) {
        return ApiResponse.<ItineraryResponse>builder()
                .data(itineraryService.updateItinerary(itineraryId, request))
                .build();
    }

    @Operation(summary = "Delete an itinerary")
    @DeleteMapping(Endpoint.Itinerary.ID)
    public ApiResponse<Void> deleteItinerary(@PathVariable UUID itineraryId) {
        itineraryService.deleteItinerary(itineraryId);
        return ApiResponse.<Void>builder()
                .message("Itinerary deleted successfully")
                .build();
    }

    // --- Favorite Actions ---

    @Operation(summary = "Favorite an itinerary")
    @PostMapping(Endpoint.Itinerary.FAVORITES)
    public ApiResponse<Void> favoriteItinerary(@PathVariable UUID itineraryId) {
        itineraryService.favoriteItinerary(itineraryId);
        return ApiResponse.<Void>builder().message("Itinerary favorited").build();
    }

    @Operation(summary = "Unfavorite an itinerary")
    @DeleteMapping(Endpoint.Itinerary.FAVORITES)
    public ApiResponse<Void> unfavoriteItinerary(@PathVariable UUID itineraryId) {
        itineraryService.unfavoriteItinerary(itineraryId);
        return ApiResponse.<Void>builder().message("Itinerary unfavorited").build();
    }

    // --- Trip Items ---

    @Operation(summary = "Add a trip item to an itinerary")
    @PostMapping(Endpoint.Itinerary.ITEMS_BASE)
    public ApiResponse<TripItemResponse> addTripItem(
            @PathVariable("itineraryId") UUID itineraryId, @Valid @RequestBody TripItemRequest request) {
        return ApiResponse.<TripItemResponse>builder()
                .data(itineraryService.addTripItem(itineraryId, request))
                .build();
    }

    @Operation(summary = "Get all trip items for an itinerary")
    @GetMapping(Endpoint.Itinerary.ITEMS_BASE)
    public ApiResponse<List<TripItemResponse>> getTripItems(@PathVariable("itineraryId") UUID itineraryId) {
        return ApiResponse.<List<TripItemResponse>>builder()
                .data(itineraryService.getTripItems(itineraryId))
                .build();
    }

    @Operation(summary = "Update a specific trip item")
    @PutMapping(Endpoint.Itinerary.ITEMS_ID)
    public ApiResponse<TripItemResponse> updateTripItem(
            @PathVariable("itineraryId") UUID itineraryId,
            @PathVariable("tripItemId") UUID tripItemId,
            @Valid @RequestBody TripItemRequest request) {
        return ApiResponse.<TripItemResponse>builder()
                .data(itineraryService.updateTripItem(itineraryId, tripItemId, request))
                .build();
    }

    @Operation(summary = "Remove a specific trip item from an itinerary")
    @DeleteMapping(Endpoint.Itinerary.ITEMS_ID)
    public ApiResponse<Void> removeTripItem(
            @PathVariable("itineraryId") UUID itineraryId, @PathVariable("tripItemId") UUID tripItemId) {
        itineraryService.removeTripItem(itineraryId, tripItemId);
        return ApiResponse.<Void>builder().message("Trip item removed").build();
    }

    // --- AI Modification ---

    @Operation(summary = "AI Modify Itinerary — replace unwanted locations with AI-suggested alternatives", description = "Provide a list of Google Place IDs you don't want. "
            + "The AI will pick suitable replacements and update the itinerary synchronously.")
    @PostMapping(Endpoint.Itinerary.AI_MODIFY)
    public ResponseEntity<ApiResponse<ItineraryResponse>> aiModifyItinerary(
            @PathVariable UUID itineraryId,
            @Valid @RequestBody AiModifyItineraryRequest request) {

        ItineraryResponse response = itineraryGenerationService.modifyItinerary(itineraryId, request);

        return ResponseEntity.ok(
                ApiResponse.<ItineraryResponse>builder()
                        .message("Itinerary has been successfully modified by AI")
                        .data(response)
                        .build());
    }
}
