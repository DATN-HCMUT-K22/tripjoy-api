package com.tripjoy.api.service;

import java.util.List;
import java.util.UUID;

import com.tripjoy.api.dto.request.ExpenseRequest;
import com.tripjoy.api.dto.request.ItineraryRequest;
import com.tripjoy.api.dto.request.TripItemRequest;
import com.tripjoy.api.dto.response.ExpenseResponse;
import com.tripjoy.api.dto.response.ItineraryResponse;
import com.tripjoy.api.dto.response.TripItemResponse;

public interface IItineraryService {
    
    // --- Itinerary CRUD ---
    ItineraryResponse createItinerary(ItineraryRequest request);
    
    ItineraryResponse getItineraryById(UUID id);
    
    ItineraryResponse updateItinerary(UUID id, ItineraryRequest request);
    
    void deleteItinerary(UUID id);
    
    List<ItineraryResponse> getMyItineraries();
    
    List<ItineraryResponse> getMyFavoriteItineraries();
    
    // --- Favorite Actions ---
    void favoriteItinerary(UUID id);
    
    void unfavoriteItinerary(UUID id);
    
    // --- Trip Items ---
    TripItemResponse addTripItem(UUID itineraryId, TripItemRequest request);
    
    List<TripItemResponse> getTripItems(UUID itineraryId);
    
    TripItemResponse updateTripItem(UUID itineraryId, UUID tripItemId, TripItemRequest request);
    
    void removeTripItem(UUID itineraryId, UUID tripItemId);
    
    // --- Expenses ---
    ExpenseResponse addExpense(UUID itineraryId, ExpenseRequest request);
    
    List<ExpenseResponse> getExpenses(UUID itineraryId);
    
    ExpenseResponse updateExpense(UUID itineraryId, UUID expenseId, ExpenseRequest request);
    
    void removeExpense(UUID itineraryId, UUID expenseId);
}
