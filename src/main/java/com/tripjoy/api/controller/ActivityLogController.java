package com.tripjoy.api.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.tripjoy.api.constant.Endpoint;
import com.tripjoy.api.dto.response.ActivityLogResponse;
import com.tripjoy.api.dto.response.ApiResponse;
import com.tripjoy.api.enums.ActivityAction;
import com.tripjoy.api.service.IActivityLogService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 * REST API for querying activity logs.
 *
 * <h2>Endpoint design</h2>
 * <pre>
 * GET  /api/v1/activity-logs/me                          → own logs (any authenticated user)
 * GET  /api/v1/activity-logs                             → all logs, filterable (admin)
 * GET  /api/v1/activity-logs/users/{userId}              → one user's logs (admin)
 * GET  /api/v1/activity-logs/entities/{type}/{id}        → entity audit trail (admin)
 * </pre>
 *
 * <p>All list endpoints support standard Spring {@code Pageable} query parameters:
 * {@code ?page=0&size=20&sort=createdAt,desc}. The {@code action} query parameter
 * can be used on all paginated endpoints to filter by a specific {@link ActivityAction}.
 */
@RestController
@RequestMapping(Endpoint.ActivityLog.BASE)
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Activity Log", description = "Endpoints for querying user and system activity logs")
public class ActivityLogController {

    IActivityLogService activityLogService;

    // =========================================================================
    // USER — own logs
    // =========================================================================

    /**
     * Returns the activity history of the currently authenticated user.
     *
     * <p>Example requests:
     * <pre>
     *   GET /api/v1/activity-logs/me
     *   GET /api/v1/activity-logs/me?action=POST_CREATED&page=0&size=20
     *   GET /api/v1/activity-logs/me?sort=createdAt,desc
     * </pre>
     */
    @Operation(
            summary = "Get my activity logs",
            description = """
                    Returns the paginated activity history for the currently authenticated user.
                    Optionally filter by action type using the `action` query parameter.
                    Supports standard Spring pagination: `page`, `size`, `sort`.
                    """)
    @GetMapping(Endpoint.ActivityLog.ME)
    public ApiResponse<Page<ActivityLogResponse>> getMyLogs(
            @Parameter(description = "Filter by action type (optional)")
            @RequestParam(required = false) ActivityAction action,
            Pageable pageable) {
        return ApiResponse.<Page<ActivityLogResponse>>builder()
                .data(activityLogService.getMyLogs(action, pageable))
                .build();
    }

    // =========================================================================
    // SYSTEM_ADMIN — system-wide queries
    // =========================================================================

    /**
     * Returns all activity logs across the entire system (admin only).
     *
     * <p>Example requests:
     * <pre>
     *   GET /api/v1/activity-logs
     *   GET /api/v1/activity-logs?action=USER_LOGIN&page=0&size=50
     * </pre>
     */
    @Operation(
            summary = "[Admin] Get all activity logs",
            description = """
                    Returns a paginated list of all activity logs in the system.
                    Optionally filter by action type.
                    **Requires SYSTEM_ADMIN role.**
                    """)
    @GetMapping
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ApiResponse<Page<ActivityLogResponse>> getAllLogs(
            @Parameter(description = "Filter by action type (optional)")
            @RequestParam(required = false) ActivityAction action,
            Pageable pageable) {
        return ApiResponse.<Page<ActivityLogResponse>>builder()
                .data(activityLogService.getAllLogs(action, pageable))
                .build();
    }

    /**
     * Returns the activity history of a specific user (admin only).
     *
     * <p>Example requests:
     * <pre>
     *   GET /api/v1/activity-logs/users/{userId}
     *   GET /api/v1/activity-logs/users/{userId}?action=GROUP_CREATED
     * </pre>
     */
    @Operation(
            summary = "[Admin] Get activity logs for a specific user",
            description = """
                    Returns the paginated activity history for a given user.
                    Optionally filter by action type.
                    **Requires SYSTEM_ADMIN role.**
                    """)
    @GetMapping(Endpoint.ActivityLog.USER)
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ApiResponse<Page<ActivityLogResponse>> getUserLogs(
            @PathVariable UUID userId,
            @Parameter(description = "Filter by action type (optional)")
            @RequestParam(required = false) ActivityAction action,
            Pageable pageable) {
        return ApiResponse.<Page<ActivityLogResponse>>builder()
                .data(activityLogService.getUserLogs(userId, action, pageable))
                .build();
    }

    /**
     * Returns the full chronological audit trail for a specific entity (admin only).
     *
     * <p>Example requests:
     * <pre>
     *   GET /api/v1/activity-logs/entities/POST/{postId}
     *   GET /api/v1/activity-logs/entities/GROUP/{groupId}
     * </pre>
     *
     * <p>Supported entity types: {@code POST}, {@code COMMENT}, {@code GROUP},
     * {@code ITINERARY}, {@code CONVERSATION}, {@code MESSAGE}, {@code USER}.
     */
    @Operation(
            summary = "[Admin] Get entity audit trail",
            description = """
                    Returns the complete chronological log of all actions performed on
                    a specific entity (e.g., a particular Post or Group).
                    **Requires SYSTEM_ADMIN role.**
                    """)
    @GetMapping(Endpoint.ActivityLog.ENTITY)
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ApiResponse<List<ActivityLogResponse>> getEntityAuditTrail(
            @Parameter(description = "Entity type, e.g. POST, GROUP, COMMENT, ITINERARY")
            @PathVariable String entityType,
            @Parameter(description = "The string ID of the entity")
            @PathVariable String entityId) {
        return ApiResponse.<List<ActivityLogResponse>>builder()
                .data(activityLogService.getEntityAuditTrail(entityType.toUpperCase(), entityId))
                .build();
    }
}
