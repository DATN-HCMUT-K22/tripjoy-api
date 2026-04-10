package com.tripjoy.api.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import com.tripjoy.api.dto.request.ExpenseRequest;
import com.tripjoy.api.dto.response.ExpenseResponse;
import com.tripjoy.api.entity.Expense;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ExpenseMapper {

    Expense toExpense(ExpenseRequest request);

    ExpenseResponse toExpenseResponse(Expense expense);

    void updateExpense(@MappingTarget Expense expense, ExpenseRequest request);
}
