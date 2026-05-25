package com.tripjoy.api.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tripjoy.api.dto.request.ExpenseRequest;
import com.tripjoy.api.dto.response.ExpenseResponse;
import com.tripjoy.api.dto.response.ExpenseSummaryResponse;
import com.tripjoy.api.dto.response.UserExpenseSummary;
import com.tripjoy.api.dto.response.simple.UserSimpleResponse;
import com.tripjoy.api.entity.Expense;
import com.tripjoy.api.entity.Itinerary;
import com.tripjoy.api.entity.TripItem;
import com.tripjoy.api.entity.User;
import com.tripjoy.api.exception.AppException;
import com.tripjoy.api.exception.ErrorCode;
import com.tripjoy.api.mapper.ExpenseMapper;
import com.tripjoy.api.mapper.UserMapper;
import com.tripjoy.api.repository.ExpenseRepository;
import com.tripjoy.api.repository.ItineraryRepository;
import com.tripjoy.api.repository.TripItemRepository;
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
    TripItemRepository tripItemRepository;
    UserMapper userMapper;

    @Override
    public ExpenseResponse addExpense(UUID itineraryId, ExpenseRequest request) {
        Itinerary itinerary = itineraryRepository
                .findById(itineraryId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        validateOwnership(itinerary);

        User creator = userRepository
                .findById(SecurityUtils.getCurrentUserId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // Resolve who actually paid — defaults to the creator if not specified
        User paidBy = resolvePaidBy(request.getPaidById(), creator, itinerary);

        // Resolve optional TripItem association
        TripItem tripItem = resolveTripItem(request.getTripItemId(), itineraryId);

        Expense expense = expenseMapper.toExpense(request);
        expense.setItinerary(itinerary);
        expense.setUser(creator);
        expense.setPaidBy(paidBy);
        expense.setPaidAt(request.getPaidAt() != null ? request.getPaidAt() : LocalDateTime.now());
        expense.setTripItem(tripItem);

        expense = expenseRepository.save(expense);
        return expenseMapper.toExpenseResponse(expense);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExpenseResponse> getExpenses(UUID itineraryId, UUID paidById) {
        itineraryRepository
                .findById(itineraryId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        List<Expense> expenses = (paidById != null)
                ? expenseRepository.findByItineraryIdAndPaidById(itineraryId, paidById)
                : expenseRepository.findByItineraryId(itineraryId);

        return expenses.stream()
                .map(expenseMapper::toExpenseResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ExpenseResponse updateExpense(UUID itineraryId, UUID expenseId, ExpenseRequest request) {
        Itinerary itinerary = itineraryRepository
                .findById(itineraryId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        validateOwnership(itinerary);

        Expense expense = expenseRepository
                .findById(expenseId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        if (!expense.getItinerary().getId().equals(itineraryId)) {
            throw new AppException(ErrorCode.RESOURCE_NOT_FOUND);
        }

        expenseMapper.updateExpense(expense, request);

        // Update paidBy only when explicitly provided
        if (request.getPaidById() != null) {
            User creator = userRepository
                    .findById(SecurityUtils.getCurrentUserId())
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
            expense.setPaidBy(resolvePaidBy(request.getPaidById(), creator, itinerary));
        }

        // Update paidAt only when explicitly provided
        if (request.getPaidAt() != null) {
            expense.setPaidAt(request.getPaidAt());
        }

        // Update tripItem association only when explicitly provided
        if (request.getTripItemId() != null) {
            expense.setTripItem(resolveTripItem(request.getTripItemId(), itineraryId));
        }

        expense = expenseRepository.save(expense);
        return expenseMapper.toExpenseResponse(expense);
    }

    @Override
    public void removeExpense(UUID itineraryId, UUID expenseId) {
        Itinerary itinerary = itineraryRepository
                .findById(itineraryId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        validateOwnership(itinerary);

        Expense expense = expenseRepository
                .findById(expenseId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        if (!expense.getItinerary().getId().equals(itineraryId)) {
            throw new AppException(ErrorCode.RESOURCE_NOT_FOUND);
        }

        expenseRepository.delete(expense);
    }

    @Override
    @Transactional(readOnly = true)
    public ExpenseSummaryResponse getExpenseSummary(UUID itineraryId) {
        itineraryRepository
                .findById(itineraryId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        BigDecimal grandTotal = expenseRepository
                .sumAmountByItineraryId(itineraryId)
                .orElse(BigDecimal.ZERO);

        // Raw aggregate: [UUID payerId, BigDecimal total, Long count]
        List<Object[]> rawSummary = expenseRepository.findPayerSummaryByItineraryId(itineraryId);

        // Batch-load all referenced user IDs in one query to avoid N+1
        List<UUID> payerIds = rawSummary.stream()
                .map(row -> (UUID) row[0])
                .collect(Collectors.toList());

        Map<UUID, User> userMap = userRepository.findAllById(payerIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        List<UserExpenseSummary> userSummaries = new ArrayList<>();
        for (Object[] row : rawSummary) {
            UUID payerId = (UUID) row[0];
            BigDecimal totalPaid = (BigDecimal) row[1];
            Long count = (Long) row[2];

            User payer = userMap.get(payerId);
            if (payer == null) continue; // defensive guard

            UserSimpleResponse userSimple = userMapper.toUserSimpleResponse(payer);
            userSummaries.add(UserExpenseSummary.builder()
                    .user(userSimple)
                    .totalPaid(totalPaid)
                    .expenseCount(count)
                    .build());
        }

        return ExpenseSummaryResponse.builder()
                .totalAmount(grandTotal)
                .userSummaries(userSummaries)
                .build();
    }

    // ─── Private Helpers ─────────────────────────────────────────────────────────

    /**
     * Resolves the actual payer. If {@code paidById} is null, defaults to the creator.
     * Validates that the specified payer is a member of the itinerary (personal or group).
     */
    private User resolvePaidBy(UUID paidById, User creator, Itinerary itinerary) {
        if (paidById == null || paidById.equals(creator.getId())) {
            return creator;
        }

        User paidByUser = userRepository
                .findById(paidById)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // Validate the specified payer is a member of the itinerary
        boolean isMember = isMemberOf(itinerary, paidById);
        if (!isMember) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST,
                    "The specified payer is not a member of this itinerary");
        }
        return paidByUser;
    }

    /**
     * Resolves an optional TripItem association.
     * Validates that the TripItem belongs to the same itinerary.
     */
    private TripItem resolveTripItem(UUID tripItemId, UUID itineraryId) {
        if (tripItemId == null) return null;

        TripItem tripItem = tripItemRepository
                .findById(tripItemId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        if (!tripItem.getItinerary().getId().equals(itineraryId)) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST,
                    "The specified trip item does not belong to this itinerary");
        }
        return tripItem;
    }

    private void validateOwnership(Itinerary itinerary) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        if (isMemberOf(itinerary, currentUserId)) return;
        throw new AppException(ErrorCode.UNAUTHORIZED);
    }

    private boolean isMemberOf(Itinerary itinerary, UUID userId) {
        // 1. Personal itinerary
        if (itinerary.getUser() != null && itinerary.getUser().getId().equals(userId)) {
            return true;
        }
        // 2. Group itinerary
        if (itinerary.getGroup() != null) {
            return itinerary.getGroup().getMembers().stream()
                    .anyMatch(member -> member.getUser().getId().equals(userId));
        }
        return false;
    }
}
