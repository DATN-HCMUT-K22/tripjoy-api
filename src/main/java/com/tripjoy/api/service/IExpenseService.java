package com.tripjoy.api.service;

import java.util.List;
import java.util.UUID;

import com.tripjoy.api.dto.request.ExpenseRequest;
import com.tripjoy.api.dto.response.ExpenseResponse;
import com.tripjoy.api.dto.response.ExpenseSummaryResponse;

public interface IExpenseService {

    ExpenseResponse addExpense(UUID itineraryId, ExpenseRequest request);

    /**
     * Get expenses for an itinerary, optionally filtered by the payer.
     *
     * @param itineraryId the itinerary to query
     * @param paidById    filter by this user's ID; pass {@code null} to return all expenses
     */
    List<ExpenseResponse> getExpenses(UUID itineraryId, UUID paidById);

    ExpenseResponse updateExpense(UUID itineraryId, UUID expenseId, ExpenseRequest request);

    void removeExpense(UUID itineraryId, UUID expenseId);

    /**
     * Get aggregated expense summary: grand total + per-member breakdown of who paid what.
     *
     * @param itineraryId the itinerary to summarise
     */
    ExpenseSummaryResponse getExpenseSummary(UUID itineraryId);
}
