package com.tripjoy.api.service.impl;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tripjoy.api.dto.request.ExpenseRequest;
import com.tripjoy.api.dto.response.ExpenseResponse;
import com.tripjoy.api.entity.Expense;
import com.tripjoy.api.entity.Itinerary;
import com.tripjoy.api.entity.User;
import com.tripjoy.api.exception.AppException;
import com.tripjoy.api.exception.ErrorCode;
import com.tripjoy.api.mapper.ExpenseMapper;
import com.tripjoy.api.repository.ExpenseRepository;
import com.tripjoy.api.repository.ItineraryRepository;
import com.tripjoy.api.repository.UserRepository;
import com.tripjoy.api.service.IExpenseService;
import com.tripjoy.api.utils.SecurityUtils;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
public class ExpenseService implements IExpenseService {

    ExpenseRepository expenseRepository;
    ExpenseMapper expenseMapper;
    ItineraryRepository itineraryRepository;
    UserRepository userRepository;

    @Override
    public ExpenseResponse addExpense(UUID itineraryId, ExpenseRequest request) {
        Itinerary itinerary = itineraryRepository.findById(itineraryId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        validateOwnership(itinerary);

        User user = userRepository.findById(SecurityUtils.getCurrentUserId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Expense expense = expenseMapper.toExpense(request);
        expense.setItinerary(itinerary);
        expense.setUser(user);

        expense = expenseRepository.save(expense);
        return expenseMapper.toExpenseResponse(expense);
    }

    @Override
    public List<ExpenseResponse> getExpenses(UUID itineraryId) {
        itineraryRepository.findById(itineraryId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        return expenseRepository.findByItineraryId(itineraryId).stream()
                .map(expenseMapper::toExpenseResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ExpenseResponse updateExpense(UUID itineraryId, UUID expenseId, ExpenseRequest request) {
        Itinerary itinerary = itineraryRepository.findById(itineraryId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        validateOwnership(itinerary);

        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        if (!expense.getItinerary().getId().equals(itineraryId)) {
            throw new AppException(ErrorCode.RESOURCE_NOT_FOUND);
        }

        expenseMapper.updateExpense(expense, request);

        expense = expenseRepository.save(expense);
        return expenseMapper.toExpenseResponse(expense);
    }

    @Override
    public void removeExpense(UUID itineraryId, UUID expenseId) {
        Itinerary itinerary = itineraryRepository.findById(itineraryId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        validateOwnership(itinerary);

        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        if (!expense.getItinerary().getId().equals(itineraryId)) {
            throw new AppException(ErrorCode.RESOURCE_NOT_FOUND);
        }

        expenseRepository.delete(expense);
    }

    private void validateOwnership(Itinerary itinerary) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        // 1. Personal Itinerary
        if (itinerary.getUser() != null && itinerary.getUser().getId().equals(currentUserId)) {
            return;
        }
        // 2. Group Itinerary
        if (itinerary.getGroup() != null) {
            boolean isInGroup = itinerary.getGroup().getMembers().stream()
                    .anyMatch(member -> member.getUser().getId().equals(currentUserId));
            if (isInGroup) return;
        }
        
        throw new AppException(ErrorCode.UNAUTHORIZED);
    }
}
