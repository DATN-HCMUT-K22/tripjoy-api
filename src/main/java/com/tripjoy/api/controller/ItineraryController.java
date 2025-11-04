package com.tripjoy.api.controller;

import com.tripjoy.api.constant.Endpoint;
import com.tripjoy.api.dto.request.ItineraryRequest;
import com.tripjoy.api.dto.request.TripItemRequest;
import com.tripjoy.api.dto.response.ApiResponse;
import com.tripjoy.api.dto.response.ItineraryResponse;
import com.tripjoy.api.dto.response.TripItemResponse;
import com.tripjoy.api.service.ItineraryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(Endpoint.Itinerary.BASE)
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Itinerary", description = "Endpoints for managing itineraries and trip items")
public class ItineraryController {

    ItineraryService itineraryService;

    @Operation(summary = "Create a new itinerary")
    @PostMapping
    public ApiResponse<ItineraryResponse> createItinerary(@Valid @RequestBody ItineraryRequest request) {
        return ApiResponse.<ItineraryResponse>builder()
//                .data(itineraryService.createItinerary(request))
                .build();
    }

    @Operation(summary = "Get a single itinerary by ID")
    @GetMapping(Endpoint.Itinerary.ID)
    public ApiResponse<ItineraryResponse> getItineraryById(@PathVariable String itineraryId) {
        return ApiResponse.<ItineraryResponse>builder()
//                .data(itineraryService.getItineraryById(itineraryId))
                .build();
    }

    @Operation(summary = "Update an itinerary")
    @PutMapping(Endpoint.Itinerary.ID)
    public ApiResponse<ItineraryResponse> updateItinerary(@PathVariable String itineraryId, @Valid @RequestBody ItineraryRequest request) {
        return ApiResponse.<ItineraryResponse>builder()
//                .data(itineraryService.updateItinerary(itineraryId, request))
                .build();
    }

    @Operation(summary = "Delete an itinerary")
    @DeleteMapping(Endpoint.Itinerary.ID)
    public ApiResponse<Void> deleteItinerary(@PathVariable String itineraryId) {
//        itineraryService.deleteItinerary(itineraryId);
        return ApiResponse.<Void>builder().message("Itinerary deleted successfully").build();
    }

    // --- Favorite Actions ---

    @Operation(summary = "Favorite an itinerary")
    @PostMapping(Endpoint.Itinerary.FAVORITE)
    public ApiResponse<Void> favoriteItinerary(@PathVariable String itineraryId) {
//        itineraryService.favoriteItinerary(itineraryId);
        return ApiResponse.<Void>builder().message("Itinerary favorited").build();
    }

    @Operation(summary = "Unfavorite an itinerary")
    @DeleteMapping(Endpoint.Itinerary.FAVORITE)
    public ApiResponse<Void> unfavoriteItinerary(@PathVariable String itineraryId) {
//        itineraryService.unfavoriteItinerary(itineraryId);
        return ApiResponse.<Void>builder().message("Itinerary unfavorited").build();
    }

    // --- Nested Trip Items ---

    @Operation(summary = "Add a trip item to an itinerary")
    @PostMapping(Endpoint.Itinerary.ITEMS_BASE)
    public ApiResponse<TripItemResponse> addTripItem(@PathVariable String itineraryId, @Valid @RequestBody TripItemRequest request) {
        return ApiResponse.<TripItemResponse>builder()
//                .data(itineraryService.addTripItem(itineraryId, request))
                .build();
    }

    @Operation(summary = "Get all trip items for an itinerary")
    @GetMapping(Endpoint.Itinerary.ITEMS_BASE)
    public ApiResponse<List<TripItemResponse>> getTripItems(@PathVariable String itineraryId) {
        return ApiResponse.<List<TripItemResponse>>builder()
//                .data(itineraryService.getTripItems(itineraryId))
                .build();
    }

    @Operation(summary = "Update a specific trip item")
    @PutMapping(Endpoint.Itinerary.ITEMS_ID)
    public ApiResponse<TripItemResponse> updateTripItem(@PathVariable String itineraryId, @PathVariable String tripItemId, @Valid @RequestBody TripItemRequest request) {
        return ApiResponse.<TripItemResponse>builder()
//                .data(itineraryService.updateTripItem(itineraryId, tripItemId, request))
                .build();
    }

    @Operation(summary = "Remove a specific trip item from an itinerary")
    @DeleteMapping(Endpoint.Itinerary.ITEMS_ID)
    public ApiResponse<Void> removeTripItem(@PathVariable String itineraryId, @PathVariable String tripItemId) {
//        itineraryService.removeTripItem(itineraryId, tripItemId);
        return ApiResponse.<Void>builder().message("Trip item removed").build();
    }
}