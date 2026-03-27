package com.tripjoy.api.controller;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;

import com.tripjoy.api.constant.Endpoint;
import com.tripjoy.api.dto.request.ExpenseRequest;
import com.tripjoy.api.dto.request.ItineraryRequest;
import com.tripjoy.api.dto.request.TripItemRequest;
import com.tripjoy.api.dto.request.GenerateItineraryRequest;
import com.tripjoy.api.dto.response.*;
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
    @PostMapping("/generate")
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
                        .build()
        );
    }

    @Operation(summary = "Create a new itinerary")
    @PostMapping
    public ApiResponse<ItineraryResponse> createItinerary(@Valid @RequestBody ItineraryRequest request) {
        return ApiResponse.<ItineraryResponse>builder()
                // .data(itineraryService.createItinerary(request))
                .build();
    }

    @Operation(summary = "Get a single itinerary by ID")
    @GetMapping(Endpoint.Itinerary.ID)
    public ApiResponse<ItineraryResponse> getItineraryById(@PathVariable UUID itineraryId) {
        return ApiResponse.<ItineraryResponse>builder()
                // .data(itineraryService.getItineraryById(itineraryId))
                .build();
    }

    @Operation(summary = "Update an itinerary")
    @PutMapping(Endpoint.Itinerary.ID)
    public ApiResponse<ItineraryResponse> updateItinerary(
            @PathVariable UUID itineraryId, @Valid @RequestBody ItineraryRequest request) {
        return ApiResponse.<ItineraryResponse>builder()
                // .data(itineraryService.updateItinerary(itineraryId, request))
                .build();
    }

    @Operation(summary = "Delete an itinerary")
    @DeleteMapping(Endpoint.Itinerary.ID)
    public ApiResponse<Void> deleteItinerary(@PathVariable UUID itineraryId) {
        // itineraryService.deleteItinerary(itineraryId);
        return ApiResponse.<Void>builder()
                .message("Itinerary deleted successfully")
                .build();
    }

    // --- Favorite Actions ---

    @Operation(summary = "Favorite an itinerary")
    @PostMapping(Endpoint.Itinerary.FAVORITES)
    public ApiResponse<Void> favoriteItinerary(@PathVariable UUID itineraryId) {
        // itineraryService.favoriteItinerary(itineraryId);
        return ApiResponse.<Void>builder().message("Itinerary favorited").build();
    }

    @Operation(summary = "Unfavorite an itinerary")
    @DeleteMapping(Endpoint.Itinerary.FAVORITES)
    public ApiResponse<Void> unfavoriteItinerary(@PathVariable UUID itineraryId) {
        // itineraryService.unfavoriteItinerary(itineraryId);
        return ApiResponse.<Void>builder().message("Itinerary unfavorited").build();
    }

    // --- Trip Items ---

    @Operation(summary = "Add a trip item to an itinerary")
    @PostMapping(Endpoint.Itinerary.ITEMS_BASE)
    public ApiResponse<TripItemResponse> addTripItem(
            @PathVariable UUID itineraryId, @Valid @RequestBody TripItemRequest request) {
        return ApiResponse.<TripItemResponse>builder()
                // .data(itineraryService.addTripItem(itineraryId, request))
                .build();
    }

    @Operation(summary = "Get all trip items for an itinerary")
    @GetMapping(Endpoint.Itinerary.ITEMS_BASE)
    public ApiResponse<List<TripItemResponse>> getTripItems(@PathVariable UUID itineraryId) {
        return ApiResponse.<List<TripItemResponse>>builder()
                // .data(itineraryService.getTripItems(itineraryId))
                .build();
    }

    @Operation(summary = "Update a specific trip item")
    @PutMapping(Endpoint.Itinerary.ITEMS_ID)
    public ApiResponse<TripItemResponse> updateTripItem(
            @PathVariable UUID itineraryId,
            @PathVariable UUID tripItemId,
            @Valid @RequestBody TripItemRequest request) {
        return ApiResponse.<TripItemResponse>builder()
                // .data(itineraryService.updateTripItem(itineraryId, tripItemId, request))
                .build();
    }

    @Operation(summary = "Remove a specific trip item from an itinerary")
    @DeleteMapping(Endpoint.Itinerary.ITEMS_ID)
    public ApiResponse<Void> removeTripItem(@PathVariable UUID itineraryId, @PathVariable UUID tripItemId) {
        // itineraryService.removeTripItem(itineraryId, tripItemId);
        return ApiResponse.<Void>builder().message("Trip item removed").build();
    }

    // --- Itinerary Expenses ---

    @Operation(summary = "Add an expense to the itinerary")
    @PostMapping(Endpoint.Itinerary.EXPENSES_BASE)
    public ApiResponse<ExpenseResponse> addExpense(
            @PathVariable UUID itineraryId, @Valid @RequestBody ExpenseRequest request) {

        // return ApiResponse.<ItineraryExpenseResponse>builder()
        // .data(itineraryService.addExpense(itineraryId, request))
        // .build();
        return null; // Placeholder
    }

    @Operation(summary = "Get all expenses for the itinerary")
    @GetMapping(Endpoint.Itinerary.EXPENSES_BASE)
    public ApiResponse<List<ExpenseResponse>> getExpenses(@PathVariable UUID itineraryId) {

        // return ApiResponse.<List<ItineraryExpenseResponse>>builder()
        // .data(itineraryService.getExpenses(itineraryId))
        // .build();
        return null; // Placeholder
    }

    @Operation(summary = "Update a specific expense entry")
    @PutMapping(Endpoint.Itinerary.EXPENSES_ID)
    public ApiResponse<ExpenseResponse> updateExpense(
            @PathVariable UUID itineraryId,
            @PathVariable UUID expenseId, // This is the ID from 'Add_expense' table
            @Valid @RequestBody ExpenseRequest request) {

        // return ApiResponse.<ItineraryExpenseResponse>builder()
        // .data(itineraryService.updateExpense(itineraryId, expenseId, request))
        // .build();
        return null; // Placeholder
    }

    @Operation(summary = "Remove an expense entry")
    @DeleteMapping(Endpoint.Itinerary.EXPENSES_ID)
    public ApiResponse<Void> removeExpense(
            @PathVariable UUID itineraryId, @PathVariable UUID expenseId) { // This is the ID from 'Add_expense' table

        // itineraryService.removeExpense(itineraryId, expenseId);
        // return ApiResponse.<Void>builder().message("Expense removed").build();
        return null; // Placeholder
    }

    // --- Itinerary Notebooks (List) ---

    @Operation(summary = "Get all travel notebooks for this itinerary")
    @GetMapping(Endpoint.Itinerary.NOTEBOOKS)
    public ApiResponse<List<TravelNotebookResponse>> getNotebooksForItinerary(@PathVariable UUID itineraryId) {

        // return ApiResponse.<List<TravelNotebookResponse>>builder()
        // .data(travelNotebookService.getNotebooksByItinerary(itineraryId))
        // .build();
        return null; // Placeholder
    }
}
