package com.tripjoy.api.service.impl;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tripjoy.api.dto.response.ActivityLogResponse;
import com.tripjoy.api.dto.response.simple.UserSimpleResponse;
import com.tripjoy.api.entity.ActivityLog;
import com.tripjoy.api.entity.User;
import com.tripjoy.api.enums.ActivityAction;
import com.tripjoy.api.exception.AppException;
import com.tripjoy.api.exception.ErrorCode;
import com.tripjoy.api.repository.ActivityLogRepository;
import com.tripjoy.api.repository.UserRepository;
import com.tripjoy.api.service.IActivityLogService;
import com.tripjoy.api.utils.SecurityUtils;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

/**
 * Core implementation of the Activity Log service.
 *
 * <p><b>Write path</b>: The {@link #log} method uses {@code REQUIRES_NEW} propagation
 * so that each log entry is committed in its own isolated transaction. This ensures
 * that a failure in the calling transaction does not prevent the audit record from
 * being persisted, and vice versa.
 *
 * <p><b>Error isolation</b>: The write path is fully wrapped in a try-catch block.
 * Logging must NEVER interfere with the main business flow.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ActivityLogService implements IActivityLogService {

    ActivityLogRepository activityLogRepository;
    UserRepository userRepository;
    ObjectMapper objectMapper;

    // =========================================================================
    // WRITE PATH
    // =========================================================================

    /**
     * {@inheritDoc}
     *
     * <p>Uses {@code REQUIRES_NEW} to ensure the log record is persisted in its own
     * transaction, independent of any surrounding transactional context (e.g. the
     * listener's own transaction). This guarantees audit records survive even when
     * the parent transaction rolls back or encounters an error.
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(
            User user,
            ActivityAction action,
            String entityType,
            String entityId,
            Map<String, Object> metadata,
            String ipAddress) {
        try {
            String metadataJson = serializeMetadata(metadata);

            ActivityLog entry = ActivityLog.builder()
                    .user(user)
                    .action(action)
                    .entityType(entityType)
                    .entityId(entityId)
                    .metadata(metadataJson)
                    .ipAddress(ipAddress)
                    .build();

            activityLogRepository.save(entry);

            log.debug(
                    "Activity logged: userId={}, action={}, entityType={}, entityId={}",
                    user.getId(),
                    action,
                    entityType,
                    entityId);

        } catch (Exception ex) {
            // NEVER propagate — logging must not disrupt business operations
            log.error(
                    "Failed to persist activity log: userId={}, action={}, error={}",
                    user.getId(),
                    action,
                    ex.getMessage(),
                    ex);
        }
    }

    // =========================================================================
    // READ PATH
    // =========================================================================

    @Override
    @Transactional(readOnly = true)
    public Page<ActivityLogResponse> getMyLogs(ActivityAction action, Pageable pageable) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        return fetchUserLogs(currentUserId, action, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ActivityLogResponse> getUserLogs(UUID userId, ActivityAction action, Pageable pageable) {
        // Validate user existence
        if (!userRepository.existsById(userId)) {
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }
        return fetchUserLogs(userId, action, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ActivityLogResponse> getAllLogs(ActivityAction action, Pageable pageable) {
        Page<ActivityLog> page;
        if (action != null) {
            page = activityLogRepository.findByAction(action, pageable);
        } else {
            page = activityLogRepository.findRecentActivities(pageable);
        }
        return page.map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ActivityLogResponse> getEntityAuditTrail(String entityType, String entityId) {
        return activityLogRepository.findByEntity(entityType, entityId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public long countByUser(UUID userId) {
        return activityLogRepository.countByUserId(userId);
    }

    // =========================================================================
    // PRIVATE HELPERS
    // =========================================================================

    /**
     * Shared implementation for getMyLogs and getUserLogs.
     * Applies optional action filter and maps to response DTOs.
     */
    private Page<ActivityLogResponse> fetchUserLogs(UUID userId, ActivityAction action, Pageable pageable) {
        Page<ActivityLog> page;
        if (action != null) {
            page = activityLogRepository.findByUserIdAndAction(userId, action, pageable);
        } else {
            page = activityLogRepository.findByUserId(userId, pageable);
        }
        return page.map(this::toResponse);
    }

    /**
     * Maps an {@link ActivityLog} entity to its response DTO.
     * Intentionally avoids MapStruct here to keep the mapping logic
     * transparent and avoid over-engineering for a simple flat projection.
     */
    private ActivityLogResponse toResponse(ActivityLog log) {
        User user = log.getUser();

        UserSimpleResponse userSimple = user == null
                ? null
                : UserSimpleResponse.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .fullName(user.getFullName())
                        .avatarUrl(user.getAvatarUrl())
                        .build();

        return ActivityLogResponse.builder()
                .id(log.getId())
                .user(userSimple)
                .action(log.getAction())
                .entityType(log.getEntityType())
                .entityId(log.getEntityId())
                .metadata(log.getMetadata())
                .ipAddress(log.getIpAddress())
                .createdAt(log.getCreatedAt())
                .build();
    }

    /**
     * Serializes a metadata map to a compact JSON string.
     * Returns null if the map is null or empty.
     */
    private String serializeMetadata(Map<String, Object> metadata) {
        if (metadata == null || metadata.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(metadata);
        } catch (JsonProcessingException ex) {
            log.warn("Failed to serialize activity log metadata: {}", ex.getMessage());
            return null;
        }
    }
}
