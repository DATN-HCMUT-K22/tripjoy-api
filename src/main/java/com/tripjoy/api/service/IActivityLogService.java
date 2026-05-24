package com.tripjoy.api.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.tripjoy.api.dto.response.ActivityLogResponse;
import com.tripjoy.api.entity.User;
import com.tripjoy.api.enums.ActivityAction;

/**
 * Contract for the Activity Log domain.
 *
 * <p><b>Write path</b> — called exclusively by {@code ActivityLogEventListener}.
 * All write operations are fire-and-forget; callers must never rely on the result.
 *
 * <p><b>Read path</b> — exposed via {@code ActivityLogController} for both
 * authenticated users (own logs) and administrators (all logs, user logs, entity audit trail).
 */
public interface IActivityLogService {

    // =========================================================================
    // WRITE — Internal use only (called from ActivityLogEventListener)
    // =========================================================================

    /**
     * Persist a single activity log entry.
     *
     * <p>This method is intentionally failsafe: it MUST NOT propagate any
     * exception to the caller. All error handling is internal.
     *
     * @param user       the user who performed the action (must not be null)
     * @param action     the action type
     * @param entityType the domain entity type affected (e.g. "POST", "GROUP") — nullable
     * @param entityId   the string-form ID of the affected entity — nullable
     * @param metadata   optional key-value context serialized to JSON — nullable
     * @param ipAddress  the originating IP address — nullable
     */
    void log(
            User user,
            ActivityAction action,
            String entityType,
            String entityId,
            Map<String, Object> metadata,
            String ipAddress);

    // =========================================================================
    // READ — Exposed via REST endpoints
    // =========================================================================

    /**
     * Returns a paginated list of activity logs for the currently authenticated user.
     * Optionally filtered by action type.
     *
     * @param action   optional filter — null means all actions
     * @param pageable pagination and sorting
     */
    Page<ActivityLogResponse> getMyLogs(ActivityAction action, Pageable pageable);

    /**
     * Returns a paginated list of activity logs for any given user.
     * <b>Admin only.</b>
     *
     * @param userId   the target user's ID
     * @param action   optional filter — null means all actions
     * @param pageable pagination and sorting
     */
    Page<ActivityLogResponse> getUserLogs(UUID userId, ActivityAction action, Pageable pageable);

    /**
     * Returns a paginated list of all activity logs across the system.
     * Optionally filtered by action type.
     * <b>Admin only.</b>
     *
     * @param action   optional filter — null means all actions
     * @param pageable pagination and sorting
     */
    Page<ActivityLogResponse> getAllLogs(ActivityAction action, Pageable pageable);

    /**
     * Returns the full chronological audit trail for a specific entity.
     * Useful for tracking the lifecycle of a Post, Group, etc.
     * <b>Admin only.</b>
     *
     * @param entityType the entity type string (e.g. "POST")
     * @param entityId   the entity's string ID
     */
    List<ActivityLogResponse> getEntityAuditTrail(String entityType, String entityId);

    /**
     * Returns the total number of activity log entries for a given user.
     * <b>Admin only.</b>
     */
    long countByUser(UUID userId);
}
