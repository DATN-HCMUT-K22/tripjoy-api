package com.tripjoy.api.controller;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;

import com.tripjoy.api.constant.Endpoint;
import com.tripjoy.api.dto.request.ExpenseRequest;
import com.tripjoy.api.dto.response.ApiResponse;
import com.tripjoy.api.dto.response.ExpenseResponse;
import com.tripjoy.api.service.IExpenseService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping(Endpoint.Itinerary.BASE) // /api/v1/itineraries
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Expense", description = "Endpoints for managing itinerary expenses")
public class ExpenseController {

    IExpenseService expenseService;

    @Operation(summary = "Add an expense to an itinerary")
    @PostMapping(Endpoint.Itinerary.EXPENSES_BASE)
    public ApiResponse<ExpenseResponse> addExpense(
            @PathVariable UUID itineraryId, @Valid @RequestBody ExpenseRequest request) {
        return ApiResponse.<ExpenseResponse>builder()
                .data(expenseService.addExpense(itineraryId, request))
                .build();
    }

    @Operation(summary = "Get all expenses of an itinerary")
    @GetMapping(Endpoint.Itinerary.EXPENSES_BASE)
    public ApiResponse<List<ExpenseResponse>> getExpenses(@PathVariable UUID itineraryId) {
        return ApiResponse.<List<ExpenseResponse>>builder()
                .data(expenseService.getExpenses(itineraryId))
                .build();
    }

    @Operation(summary = "Update an expense in an itinerary")
    @PutMapping(Endpoint.Itinerary.EXPENSES_ID)
    public ApiResponse<ExpenseResponse> updateExpense(
            @PathVariable UUID itineraryId,
            @PathVariable UUID expenseId,
            @Valid @RequestBody ExpenseRequest request) {
        return ApiResponse.<ExpenseResponse>builder()
                .data(expenseService.updateExpense(itineraryId, expenseId, request))
                .build();
    }

    @Operation(summary = "Remove an expense from an itinerary")
    @DeleteMapping(Endpoint.Itinerary.EXPENSES_ID)
    public ApiResponse<Void> removeExpense(@PathVariable UUID itineraryId, @PathVariable UUID expenseId) {
        expenseService.removeExpense(itineraryId, expenseId);
        return ApiResponse.<Void>builder()
                .message("Expense removed successfully")
                .build();
    }
}
