package com.tripjoy.api.service;

import java.util.List;
import java.util.UUID;

import com.tripjoy.api.dto.request.ExpenseRequest;
import com.tripjoy.api.dto.response.ExpenseResponse;

public interface IExpenseService {
    ExpenseResponse addExpense(UUID itineraryId, ExpenseRequest request);

    List<ExpenseResponse> getExpenses(UUID itineraryId);

    ExpenseResponse updateExpense(UUID itineraryId, UUID expenseId, ExpenseRequest request);

    void removeExpense(UUID itineraryId, UUID expenseId);
}
