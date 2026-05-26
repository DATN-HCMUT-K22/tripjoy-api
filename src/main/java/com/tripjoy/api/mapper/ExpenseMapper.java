package com.tripjoy.api.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import com.tripjoy.api.dto.request.ExpenseRequest;
import com.tripjoy.api.dto.response.ExpenseResponse;
import com.tripjoy.api.entity.Expense;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {UserMapper.class})
public interface ExpenseMapper {

    /**
     * Maps request fields that can be set directly.
     * paidBy, tripItem and user are resolved manually in the service layer.
     */
    @Mapping(target = "paidBy", ignore = true)
    @Mapping(target = "tripItem", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "itinerary", ignore = true)
    @Mapping(target = "paidAt", ignore = true)
    Expense toExpense(ExpenseRequest request);

    /**
     * Maps entity to response.
     * tripItemId is extracted from the associated TripItem's id.
     */
    @Mapping(target = "tripItemId", source = "tripItem.id")
    ExpenseResponse toExpenseResponse(Expense expense);

    /**
     * Partial update — only maps primitive/value fields.
     * Relationships and paidAt are handled explicitly in the service.
     */
    @Mapping(target = "paidBy", ignore = true)
    @Mapping(target = "tripItem", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "itinerary", ignore = true)
    @Mapping(target = "paidAt", ignore = true)
    void updateExpense(@MappingTarget Expense expense, ExpenseRequest request);
}
