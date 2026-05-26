package com.tripjoy.api.service.impl;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tripjoy.api.configuration.redis.RedisCacheConfig;
import com.tripjoy.api.dto.event.ItineraryCreatedEvent;
import com.tripjoy.api.dto.event.ItineraryDeletedEvent;
import com.tripjoy.api.dto.request.ItineraryRequest;
import com.tripjoy.api.dto.request.ItineraryStatusRequest;
import com.tripjoy.api.dto.request.TripItemRequest;
import com.tripjoy.api.dto.request.TripItemStatusRequest;
import com.tripjoy.api.dto.response.ItineraryResponse;
import com.tripjoy.api.dto.response.TripItemResponse;
import com.tripjoy.api.entity.Group;
import com.tripjoy.api.entity.Itinerary;
import com.tripjoy.api.entity.Location;
import com.tripjoy.api.entity.TripItem;
import com.tripjoy.api.entity.User;
import com.tripjoy.api.enums.GroupRole;
import com.tripjoy.api.enums.ItineraryStatus;
import com.tripjoy.api.exception.AppException;
import com.tripjoy.api.exception.ErrorCode;
import com.tripjoy.api.mapper.ItineraryMapper;
import com.tripjoy.api.mapper.TripItemMapper;
import com.tripjoy.api.repository.GroupMemberRepository;
import com.tripjoy.api.repository.GroupRepository;
import com.tripjoy.api.repository.ItineraryRepository;
import com.tripjoy.api.repository.LocationRepository;
import com.tripjoy.api.repository.TripItemRepository;
import com.tripjoy.api.repository.UserRepository;
import com.tripjoy.api.service.IItineraryService;
import com.tripjoy.api.service.ILocationService;
import com.tripjoy.api.service.IThemeService;
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
    UserRepository userRepository;
    GroupRepository groupRepository;
    LocationRepository locationRepository;
    ItineraryMapper itineraryMapper;
    TripItemMapper tripItemMapper;
    IThemeService themeService;
    ILocationService locationService;
    GroupMemberRepository groupMemberRepository;
    CacheManager cacheManager;
    ApplicationEventPublisher eventPublisher;

    @Override
    public ItineraryResponse createItinerary(ItineraryRequest request) {
        UUID userId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Itinerary itinerary = itineraryMapper.toItinerary(request);
        itinerary.setUser(user);

        if (request.getThemes() != null) {
            itinerary.setThemes(themeService.syncThemes(request.getThemes()));
        }

        if (request.getStatus() == null) {
            itinerary.setStatus(ItineraryStatus.DRAFT);
        }

        if (request.getGroupId() != null) {
            Group group = groupRepository
                    .findById(UUID.fromString(request.getGroupId()))
                    .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
            itinerary.setGroup(group);
        }

        // Process TripItems Cascade
        if (request.getTripItems() != null && !request.getTripItems().isEmpty()) {
            final Itinerary finalItinerary = itinerary;
            List<TripItem> tripItems = request.getTripItems().stream()
                    .map(itemReq -> {
                        TripItem item = tripItemMapper.toTripItem(itemReq);
                        item.setItinerary(finalItinerary);
                        item.setLocation(resolveLocation(itemReq));
                        return item;
                    })
                    .collect(Collectors.toList());
            itinerary.getTripItems().addAll(tripItems);
        }

        itinerary = itineraryRepository.save(itinerary);
        evictItinerariesAndGroupsCache(itinerary);

        eventPublisher.publishEvent(new ItineraryCreatedEvent(itinerary, user));

        return itineraryMapper.toItineraryResponse(itinerary);
    }

    @Override
    public ItineraryResponse getItineraryById(UUID id) {
        Itinerary itinerary =
                itineraryRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        if (itinerary.getSoftDeleteInfo().isDeleted()) {
            throw new AppException(ErrorCode.RESOURCE_NOT_FOUND);
        }

        return itineraryMapper.toItineraryResponse(itinerary);
    }

    @Override
    public ItineraryResponse updateItinerary(UUID id, ItineraryRequest request) {
        Itinerary itinerary =
                itineraryRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        validateOwnership(itinerary);

        itineraryMapper.updateItinerary(itinerary, request);

        if (request.getThemes() != null) {
            itinerary.setThemes(themeService.syncThemes(request.getThemes()));
        }

        if (request.getGroupId() != null) {
            Group group = groupRepository
                    .findById(UUID.fromString(request.getGroupId()))
                    .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
            itinerary.setGroup(group);
        }

        itinerary = itineraryRepository.save(itinerary);
        evictItinerariesAndGroupsCache(itinerary);
        return itineraryMapper.toItineraryResponse(itinerary);
    }

    @Override
    public void deleteItinerary(UUID id) {
        Itinerary itinerary =
                itineraryRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        validateOwnership(itinerary);

        UUID currentUserId = SecurityUtils.getCurrentUserId();
        User actor = userRepository.findById(currentUserId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        UUID deletedItineraryId = itinerary.getId();

        itinerary
                .getSoftDeleteInfo()
                .markAsDeleted(currentUserId.toString());

        itineraryRepository.save(itinerary);
        evictItinerariesAndGroupsCache(itinerary);

        eventPublisher.publishEvent(new ItineraryDeletedEvent(deletedItineraryId, actor));
    }

    @Override
    @Transactional(readOnly = true)
    @org.springframework.cache.annotation.Cacheable(
            value = RedisCacheConfig.CACHE_ITINERARIES_BY_USER,
            key = "T(com.tripjoy.api.utils.SecurityUtils).getCurrentUserId()")
    public List<ItineraryResponse> getMyItineraries() {
        UUID userId = SecurityUtils.getCurrentUserId();
        return itineraryRepository.findByUserId(userId).stream()
                .map(itineraryMapper::toItineraryResponse)
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItineraryResponse> getMyFavoriteItineraries() {
        UUID userId = SecurityUtils.getCurrentUserId();
        return itineraryRepository.findByFavouriteUserId(userId).stream()
                .map(itineraryMapper::toItineraryResponse)
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public void favoriteItinerary(UUID id) {
        Itinerary itinerary =
                itineraryRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        User user = userRepository
                .findById(SecurityUtils.getCurrentUserId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        itinerary.getFavouriteUsers().add(user);
        itineraryRepository.save(itinerary);
    }

    @Override
    public void unfavoriteItinerary(UUID id) {
        Itinerary itinerary =
                itineraryRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        User user = userRepository
                .findById(SecurityUtils.getCurrentUserId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        itinerary.getFavouriteUsers().remove(user);
        itineraryRepository.save(itinerary);
    }

    @Override
    public TripItemResponse addTripItem(UUID itineraryId, TripItemRequest request) {
        Itinerary itinerary = itineraryRepository
                .findById(itineraryId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        validateOwnership(itinerary);

        Location location = resolveLocation(request);

        TripItem tripItem = tripItemMapper.toTripItem(request);
        tripItem.setItinerary(itinerary);
        tripItem.setLocation(location);

        tripItem = tripItemRepository.save(tripItem);

        // Async: increment usage_count — fire-and-forget, does not affect response
        locationService.incrementUsageCount(List.of(location.getId()));

        return tripItemMapper.toTripItemResponse(tripItem);
    }

    @Override
    public List<TripItemResponse> getTripItems(UUID itineraryId) {
        itineraryRepository.findById(itineraryId).orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        return tripItemRepository.findByItineraryId(itineraryId).stream()
                .map(tripItemMapper::toTripItemResponse)
                .collect(Collectors.toList());
    }

    @Override
    public TripItemResponse updateTripItem(UUID itineraryId, UUID tripItemId, TripItemRequest request) {
        Itinerary itinerary = itineraryRepository
                .findById(itineraryId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        validateOwnership(itinerary);

        TripItem tripItem = tripItemRepository
                .findById(tripItemId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        if (!tripItem.getItinerary().getId().equals(itineraryId)) {
            throw new AppException(ErrorCode.RESOURCE_NOT_FOUND);
        }

        tripItemMapper.updateTripItem(tripItem, request);

        // Business rule: rating and review can only be set when status is CHECKED_IN
        // and only by group leader or co-leader
        if (request.getRating() != null || request.getReview() != null) {
            validateLeaderOrCoLeader(itinerary);
            if (tripItem.getStatus() != com.tripjoy.api.enums.TripItemStatus.CHECKED_IN) {
                throw new AppException(
                        ErrorCode.INVALID_REQUEST,
                        "Rating and review can only be set when the trip item status is CHECKED_IN");
            }
            if (request.getRating() != null) {
                tripItem.setRating(request.getRating());
            }
            if (request.getReview() != null) {
                tripItem.setReview(request.getReview());
            }
        }

        if (request.getLocationId() != null
                && !request.getLocationId().equals(tripItem.getLocation().getId())) {
            Location location = locationRepository
                    .findById(request.getLocationId())
                    .orElseThrow(() -> new AppException(ErrorCode.LOCATION_NOT_FOUND));
            tripItem.setLocation(location);
        } else if (request.getLocationId() == null && request.getPlaceId() != null) {
            // AI suggestion resolution
            com.tripjoy.api.dto.response.location.LocationResponse locResponse =
                    locationService.resolveByPlaceId(request.getPlaceId());
            if (!locResponse.getId().equals(tripItem.getLocation().getId())) {
                Location location = locationRepository
                        .findById(locResponse.getId())
                        .orElseThrow(() -> new AppException(ErrorCode.LOCATION_NOT_FOUND));
                tripItem.setLocation(location);
            }
        }

        tripItem = tripItemRepository.save(tripItem);
        return tripItemMapper.toTripItemResponse(tripItem);
    }

    @Override
    public void removeTripItem(UUID itineraryId, UUID tripItemId) {
        Itinerary itinerary = itineraryRepository
                .findById(itineraryId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        validateOwnership(itinerary);

        TripItem tripItem = tripItemRepository
                .findById(tripItemId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        if (!tripItem.getItinerary().getId().equals(itineraryId)) {
            throw new AppException(ErrorCode.RESOURCE_NOT_FOUND);
        }

        tripItemRepository.delete(tripItem);
    }

    @Override
    public TripItemResponse updateTripItemStatus(UUID itineraryId, UUID tripItemId, TripItemStatusRequest request) {
        Itinerary itinerary = itineraryRepository
                .findById(itineraryId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        validateOwnership(itinerary);

        TripItem tripItem = tripItemRepository
                .findById(tripItemId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        if (!tripItem.getItinerary().getId().equals(itineraryId)) {
            throw new AppException(ErrorCode.RESOURCE_NOT_FOUND);
        }

        tripItem.setStatus(request.getStatus());
        tripItem = tripItemRepository.save(tripItem);
        return tripItemMapper.toTripItemResponse(tripItem);
    }

    @Override
    public ItineraryResponse updateStatus(UUID id, ItineraryStatusRequest request) {
        Itinerary itinerary =
                itineraryRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        validateLeader(itinerary);

        ItineraryStatus oldStatus = itinerary.getStatus();
        ItineraryStatus newStatus = request.getStatus();

        if (oldStatus == newStatus) {
            return itineraryMapper.toItineraryResponse(itinerary);
        }

        // Rule: From CONFIRMED, you can only move to IN_PROGRESS or COMPLETED
        if (oldStatus == ItineraryStatus.CONFIRMED 
                && newStatus != ItineraryStatus.IN_PROGRESS 
                && newStatus != ItineraryStatus.COMPLETED) {
            throw new AppException(
                    ErrorCode.INVALID_ITINERARY_STATUS_TRANSITION,
                    "Itinerary is already CONFIRMED. It can only be moved to IN_PROGRESS or COMPLETED.");
        }

        // Rule: Only one CONFIRMED/IN_PROGRESS itinerary per group
        if (newStatus == ItineraryStatus.CONFIRMED || newStatus == ItineraryStatus.IN_PROGRESS) {
            if (itinerary.getGroup() != null) {
                boolean hasActive =
                        itineraryRepository
                                .findByGroupIdAndNotDeleted(itinerary.getGroup().getId())
                                .stream()
                                .anyMatch(i -> !i.getId().equals(id)
                                        && (i.getStatus() == ItineraryStatus.CONFIRMED
                                                || i.getStatus() == ItineraryStatus.IN_PROGRESS));
                if (hasActive) {
                    throw new AppException(ErrorCode.ACTIVE_ITINERARY_EXISTS);
                }
            }
        }

        itinerary.setStatus(newStatus);
        itinerary = itineraryRepository.save(itinerary);
        evictItinerariesAndGroupsCache(itinerary);
        return itineraryMapper.toItineraryResponse(itinerary);
    }

    private Location resolveLocation(TripItemRequest request) {
        if (request.getLocationId() != null) {
            return locationRepository
                    .findById(request.getLocationId())
                    .orElseThrow(() -> new AppException(ErrorCode.LOCATION_NOT_FOUND));
        } else if (request.getPlaceId() != null) {
            com.tripjoy.api.dto.response.location.LocationResponse locResponse =
                    locationService.resolveByPlaceId(request.getPlaceId());
            return locationRepository
                    .findById(locResponse.getId())
                    .orElseThrow(() -> new AppException(ErrorCode.LOCATION_NOT_FOUND));
        } else {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Either location_id or place_id must be provided");
        }
    }

    private void validateLeader(Itinerary itinerary) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();

        // 1. Personal Itinerary
        if (itinerary.getUser() != null && itinerary.getUser().getId().equals(currentUserId)) {
            return;
        }

        // 2. Group Itinerary
        if (itinerary.getGroup() != null) {
            boolean isLeader = itinerary.getGroup().getMembers().stream()
                    .anyMatch(member -> member.getUser().getId().equals(currentUserId)
                            && member.getRole() == GroupRole.LEADER
                            && !member.getSoftDeleteInfo().isDeleted());
            if (isLeader) return;
        }

        throw new AppException(ErrorCode.ONLY_LEADER_ALLOWED);
    }

    private void validateLeaderOrCoLeader(Itinerary itinerary) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();

        // 1. Personal Itinerary
        if (itinerary.getUser() != null && itinerary.getUser().getId().equals(currentUserId)) {
            return;
        }

        // 2. Group Itinerary
        if (itinerary.getGroup() != null) {
            boolean isLeaderOrCoLeader = itinerary.getGroup().getMembers().stream()
                    .anyMatch(member -> member.getUser().getId().equals(currentUserId)
                            && (member.getRole() == GroupRole.LEADER || member.getRole() == GroupRole.CO_LEADER)
                            && !member.getSoftDeleteInfo().isDeleted());
            if (isLeaderOrCoLeader) return;
        }

        throw new AppException(ErrorCode.ONLY_LEADER_ALLOWED, "Only group leader or co-leader is allowed to rate or review trip items");
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
                    .anyMatch(member -> member.getUser().getId().equals(currentUserId)
                            && !member.getSoftDeleteInfo().isDeleted());
            if (isInGroup) return;
        }

        throw new AppException(ErrorCode.UNAUTHORIZED);
    }

    private void evictItinerariesAndGroupsCache(Itinerary itinerary) {
        if (itinerary == null) return;

        // 1. Evict current user's cache
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        evictUserItineraryCache(currentUserId);

        // 2. If it is a group itinerary, evict itineraries and group lists cache for all active members
        if (itinerary.getGroup() != null) {
            UUID groupId = itinerary.getGroup().getId();
            List<UUID> memberUserIds = groupMemberRepository.findActiveUserIdsByGroupId(groupId);
            if (memberUserIds != null) {
                org.springframework.cache.Cache itineraryCache = cacheManager.getCache(RedisCacheConfig.CACHE_ITINERARIES_BY_USER);
                org.springframework.cache.Cache groupCache = cacheManager.getCache(RedisCacheConfig.CACHE_GROUPS_BY_USER);
                for (UUID userId : memberUserIds) {
                    if (itineraryCache != null) {
                        itineraryCache.evict(userId);
                    }
                    if (groupCache != null) {
                        groupCache.evict(userId);
                    }
                }
            }
        }
    }

    private void evictUserItineraryCache(UUID userId) {
        if (userId == null) return;
        org.springframework.cache.Cache cache = cacheManager.getCache(RedisCacheConfig.CACHE_ITINERARIES_BY_USER);
        if (cache != null) {
            cache.evict(userId);
        }
    }
}
