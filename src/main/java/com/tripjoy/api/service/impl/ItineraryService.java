package com.tripjoy.api.service.impl;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tripjoy.api.dto.request.ExpenseRequest;
import com.tripjoy.api.dto.request.ItineraryRequest;
import com.tripjoy.api.dto.request.TripItemRequest;
import com.tripjoy.api.dto.response.ExpenseResponse;
import com.tripjoy.api.dto.response.ItineraryResponse;
import com.tripjoy.api.dto.response.TripItemResponse;
import com.tripjoy.api.entity.Expense;
import com.tripjoy.api.entity.Group;
import com.tripjoy.api.entity.Itinerary;
import com.tripjoy.api.entity.Location;
import com.tripjoy.api.entity.TripItem;
import com.tripjoy.api.entity.User;
import com.tripjoy.api.enums.ItineraryStatus;
import com.tripjoy.api.exception.AppException;
import com.tripjoy.api.exception.ErrorCode;
import com.tripjoy.api.mapper.ExpenseMapper;
import com.tripjoy.api.mapper.ItineraryMapper;
import com.tripjoy.api.mapper.TripItemMapper;
import com.tripjoy.api.repository.ExpenseRepository;
import com.tripjoy.api.repository.GroupRepository;
import com.tripjoy.api.repository.ItineraryRepository;
import com.tripjoy.api.repository.LocationRepository;
import com.tripjoy.api.repository.TripItemRepository;
import com.tripjoy.api.repository.UserRepository;
import com.tripjoy.api.service.IItineraryService;
import com.tripjoy.api.utils.SecurityUtils;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
public class ItineraryService implements IItineraryService {

    ItineraryRepository itineraryRepository;
    TripItemRepository tripItemRepository;
    ExpenseRepository expenseRepository;
    UserRepository userRepository;
    GroupRepository groupRepository;
    LocationRepository locationRepository;
    ItineraryMapper itineraryMapper;
    TripItemMapper tripItemMapper;
    ExpenseMapper expenseMapper;

    @Override
    public ItineraryResponse createItinerary(ItineraryRequest request) {
        UUID userId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Itinerary itinerary = itineraryMapper.toItinerary(request);
        itinerary.setUser(user);
        
        if (request.getStatus() == null) {
            itinerary.setStatus(ItineraryStatus.DRAFT);
        }

        if (request.getGroupId() != null) {
            Group group = groupRepository.findById(UUID.fromString(request.getGroupId()))
                    .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
            itinerary.setGroup(group);
        }

        itinerary = itineraryRepository.save(itinerary);
        return itineraryMapper.toItineraryResponse(itinerary);
    }

    @Override
    public ItineraryResponse getItineraryById(UUID id) {
        Itinerary itinerary = itineraryRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
                
        if (itinerary.getSoftDeleteInfo().isDeleted()) {
             throw new AppException(ErrorCode.RESOURCE_NOT_FOUND);
        }
        
        return itineraryMapper.toItineraryResponse(itinerary);
    }

    @Override
    public ItineraryResponse updateItinerary(UUID id, ItineraryRequest request) {
        Itinerary itinerary = itineraryRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        validateOwnership(itinerary);

        itineraryMapper.updateItinerary(itinerary, request);

        if (request.getGroupId() != null) {
            Group group = groupRepository.findById(UUID.fromString(request.getGroupId()))
                    .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
            itinerary.setGroup(group);
        }

        itinerary = itineraryRepository.save(itinerary);
        return itineraryMapper.toItineraryResponse(itinerary);
    }

    @Override
    public void deleteItinerary(UUID id) {
        Itinerary itinerary = itineraryRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        validateOwnership(itinerary);

        itinerary.getSoftDeleteInfo().markAsDeleted(SecurityUtils.getCurrentUserId().toString());
        
        itineraryRepository.save(itinerary);
    }

    @Override
    public List<ItineraryResponse> getMyItineraries() {
        UUID userId = SecurityUtils.getCurrentUserId();
        return itineraryRepository.findByUserIdAndSoftDeleteInfoIsDeletedFalse(userId).stream()
                .map(itineraryMapper::toItineraryResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItineraryResponse> getMyFavoriteItineraries() {
        UUID userId = SecurityUtils.getCurrentUserId();
        return itineraryRepository.findByFavouriteUsersIdAndSoftDeleteInfoIsDeletedFalse(userId).stream()
                .map(itineraryMapper::toItineraryResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void favoriteItinerary(UUID id) {
        Itinerary itinerary = itineraryRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
                
        User user = userRepository.findById(SecurityUtils.getCurrentUserId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        itinerary.getFavouriteUsers().add(user);
        itineraryRepository.save(itinerary);
    }

    @Override
    public void unfavoriteItinerary(UUID id) {
        Itinerary itinerary = itineraryRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
                
        User user = userRepository.findById(SecurityUtils.getCurrentUserId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        itinerary.getFavouriteUsers().remove(user);
        itineraryRepository.save(itinerary);
    }

    @Override
    public TripItemResponse addTripItem(UUID itineraryId, TripItemRequest request) {
        Itinerary itinerary = itineraryRepository.findById(itineraryId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        validateOwnership(itinerary);

        Location location = locationRepository.findById(request.getLocationId())
                .orElseThrow(() -> new AppException(ErrorCode.LOCATION_NOT_FOUND));

        TripItem tripItem = tripItemMapper.toTripItem(request);
        tripItem.setItinerary(itinerary);
        tripItem.setLocation(location);

        tripItem = tripItemRepository.save(tripItem);
        return tripItemMapper.toTripItemResponse(tripItem);
    }

    @Override
    public List<TripItemResponse> getTripItems(UUID itineraryId) {
        Itinerary itinerary = itineraryRepository.findById(itineraryId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        // Note: Filter soft deleted items here if TripItem gets softDelete capability
        return itinerary.getTripItems().stream()
                .map(tripItemMapper::toTripItemResponse)
                .collect(Collectors.toList());
    }

    @Override
    public TripItemResponse updateTripItem(UUID itineraryId, UUID tripItemId, TripItemRequest request) {
        Itinerary itinerary = itineraryRepository.findById(itineraryId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        validateOwnership(itinerary);

        TripItem tripItem = tripItemRepository.findById(tripItemId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        if (!tripItem.getItinerary().getId().equals(itineraryId)) {
            throw new AppException(ErrorCode.RESOURCE_NOT_FOUND); 
        }

        tripItemMapper.updateTripItem(tripItem, request);

        if (request.getLocationId() != null && !request.getLocationId().equals(tripItem.getLocation().getId())) {
             Location location = locationRepository.findById(request.getLocationId())
                .orElseThrow(() -> new AppException(ErrorCode.LOCATION_NOT_FOUND));
             tripItem.setLocation(location);
        }

        tripItem = tripItemRepository.save(tripItem);
        return tripItemMapper.toTripItemResponse(tripItem);
    }

    @Override
    public void removeTripItem(UUID itineraryId, UUID tripItemId) {
        Itinerary itinerary = itineraryRepository.findById(itineraryId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        validateOwnership(itinerary);

        TripItem tripItem = tripItemRepository.findById(tripItemId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        if (!tripItem.getItinerary().getId().equals(itineraryId)) {
             throw new AppException(ErrorCode.RESOURCE_NOT_FOUND); 
        }

        tripItemRepository.delete(tripItem);
    }

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
        Itinerary itinerary = itineraryRepository.findById(itineraryId)
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
