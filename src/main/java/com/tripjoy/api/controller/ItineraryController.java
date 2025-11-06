package com.tripjoy.api.controller;

import com.tripjoy.api.constant.Endpoint;
import com.tripjoy.api.dto.request.ExpenseRequest;
import com.tripjoy.api.dto.request.ItineraryRequest;
import com.tripjoy.api.dto.request.TripItemRequest;
import com.tripjoy.api.dto.response.*;
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

    // --- Trip Items ---

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

    // --- Itinerary Expenses ---

    @Operation(summary = "Add an expense to the itinerary")
    @PostMapping(Endpoint.Itinerary.EXPENSES_BASE)
    public ApiResponse<ExpenseResponse> addExpense(
            @PathVariable String itineraryId,
            @Valid @RequestBody ExpenseRequest request) {

        // return ApiResponse.<ItineraryExpenseResponse>builder()
        //        .data(itineraryService.addExpense(itineraryId, request))
        //        .build();
        return null; // Placeholder
    }

    @Operation(summary = "Get all expenses for the itinerary")
    @GetMapping(Endpoint.Itinerary.EXPENSES_BASE)
    public ApiResponse<List<ExpenseResponse>> getExpenses(
            @PathVariable String itineraryId) {

        // return ApiResponse.<List<ItineraryExpenseResponse>>builder()
        //        .data(itineraryService.getExpenses(itineraryId))
        //        .build();
        return null; // Placeholder
    }

    @Operation(summary = "Update a specific expense entry")
    @PutMapping(Endpoint.Itinerary.EXPENSES_ID)
    public ApiResponse<ExpenseResponse> updateExpense(
            @PathVariable String itineraryId,
            @PathVariable String expenseId, // This is the ID from 'Add_expense' table
            @Valid @RequestBody ExpenseRequest request) {

        // return ApiResponse.<ItineraryExpenseResponse>builder()
        //        .data(itineraryService.updateExpense(itineraryId, expenseId, request))
        //        .build();
        return null; // Placeholder
    }

    @Operation(summary = "Remove an expense entry")
    @DeleteMapping(Endpoint.Itinerary.EXPENSES_ID)
    public ApiResponse<Void> removeExpense(
            @PathVariable String itineraryId,
            @PathVariable String expenseId) { // This is the ID from 'Add_expense' table

        // itineraryService.removeExpense(itineraryId, expenseId);
        // return ApiResponse.<Void>builder().message("Expense removed").build();
        return null; // Placeholder
    }

    // --- Itinerary Notebooks (List) ---

    @Operation(summary = "Get all travel notebooks for this itinerary")
    @GetMapping(Endpoint.Itinerary.NOTEBOOKS_BASE)
    public ApiResponse<List<TravelNotebookResponse>> getNotebooksForItinerary(
            @PathVariable String itineraryId) {

        // return ApiResponse.<List<TravelNotebookResponse>>builder()
        //        .data(travelNotebookService.getNotebooksByItinerary(itineraryId))
        //        .build();
        return null; // Placeholder
    }
}