package com.tripjoy.api.service.impl;

import com.tripjoy.api.dto.request.SuggestLocationRequest;
import com.tripjoy.api.dto.response.SuggestLocationResponse;
import com.tripjoy.api.entity.Group;
import com.tripjoy.api.entity.GroupMember;
import com.tripjoy.api.entity.Location;
import com.tripjoy.api.entity.SuggestLocation;
import com.tripjoy.api.entity.User;
import com.tripjoy.api.enums.GroupRole;
import com.tripjoy.api.exception.AppException;
import com.tripjoy.api.exception.ErrorCode;
import com.tripjoy.api.mapper.SuggestLocationMapper;
import com.tripjoy.api.repository.GroupMemberRepository;
import com.tripjoy.api.repository.GroupRepository;
import com.tripjoy.api.repository.LocationRepository;
import com.tripjoy.api.repository.SuggestLocationRepository;
import com.tripjoy.api.repository.UserRepository;
import com.tripjoy.api.service.ISuggestLocationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SuggestLocationService implements ISuggestLocationService {

    private final SuggestLocationRepository suggestLocationRepository;
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final LocationRepository locationRepository;
    private final UserRepository userRepository;
    private final SuggestLocationMapper suggestLocationMapper;

    @Override
    @Transactional(readOnly = true)
    public List<SuggestLocationResponse> getSuggestionsByGroup(UUID groupId, UUID currentUserId) {
        log.info("Getting suggestions for group: {}, by user: {}", groupId, currentUserId);

        // Verify group exists
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new AppException(ErrorCode.GROUP_NOT_FOUND));

        // Verify user is member of the group
        verifyUserIsMember(group, currentUserId);

        // Get all suggestions for the group
        List<SuggestLocation> suggestions = suggestLocationRepository.findByGroupIdOrderByCreatedAtDesc(groupId);

        log.info("Found {} suggestions for group: {}", suggestions.size(), groupId);
        return suggestLocationMapper.toResponseList(suggestions);
    }

    @Override
    @Transactional
    public SuggestLocationResponse createSuggestion(UUID groupId, SuggestLocationRequest request, UUID currentUserId) {
        log.info("Creating suggestion for group: {}, location: {}, by user: {}",
                groupId, request.getLocationId(), currentUserId);

        // Verify group exists
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new AppException(ErrorCode.GROUP_NOT_FOUND));

        // Verify user is member of the group
        verifyUserIsMember(group, currentUserId);

        // Verify location exists
        Location location = locationRepository.findById(request.getLocationId())
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        // Get user
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // Create suggestion
        SuggestLocation suggestion = SuggestLocation.builder()
                .group(group)
                .user(user)
                .location(location)
                .notes(request.getNotes())
                .build();

        SuggestLocation saved = suggestLocationRepository.save(suggestion);
        log.info("Created suggestion ID: {} for group: {}", saved.getId(), groupId);

        return suggestLocationMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void deleteSuggestion(UUID groupId, UUID suggestionId, UUID currentUserId) {
        log.info("Deleting suggestion: {} from group: {}, by user: {}",
                suggestionId, groupId, currentUserId);

        // Verify suggestion exists and belongs to the group
        SuggestLocation suggestion = suggestLocationRepository.findByIdAndGroupId(suggestionId, groupId)
                .orElseThrow(() -> new AppException(ErrorCode.SUGGESTION_NOT_FOUND));

        // Get user's role in group
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new AppException(ErrorCode.GROUP_NOT_FOUND));

        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        GroupMember membership = groupMemberRepository.findByGroupAndUser(group, user)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_IN_GROUP));

        // Permission check
        GroupRole role = membership.getRole();
        UUID suggestionOwnerId = suggestion.getUser().getId();

        boolean isOwner = suggestionOwnerId.equals(currentUserId);
        boolean isLeaderOrCoLeader = role == GroupRole.LEADER || role == GroupRole.CO_LEADER;

        if (!isOwner && !isLeaderOrCoLeader) {
            log.warn("User {} (role: {}) attempted to delete suggestion {} owned by {}",
                    currentUserId, role, suggestionId, suggestionOwnerId);
            throw new AppException(ErrorCode.CANNOT_DELETE_OTHERS_SUGGESTION);
        }

        // Delete suggestion (hard delete)
        suggestLocationRepository.delete(suggestion);
        log.info("Deleted suggestion: {} from group: {}", suggestionId, groupId);
    }

    /**
     * Verify that the user is a member of the group
     */
    private void verifyUserIsMember(Group group, UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        boolean isMember = groupMemberRepository.existsByGroupAndUser(group, user);
        if (!isMember) {
            throw new AppException(ErrorCode.USER_NOT_IN_GROUP);
        }
    }
}
