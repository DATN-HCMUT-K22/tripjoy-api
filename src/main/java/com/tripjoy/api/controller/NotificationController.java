package com.tripjoy.api.controller;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import com.tripjoy.api.constant.Endpoint;
import com.tripjoy.api.dto.response.ApiResponse;
import com.tripjoy.api.dto.response.NotificationResponse;
import com.tripjoy.api.service.INotificationService;
import com.tripjoy.api.utils.SecurityUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping(Endpoint.Notification.BASE)
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Notification", description = "Endpoints for managing user notifications")
public class NotificationController {

    INotificationService notificationService;

    @Operation(summary = "Get all notifications", description = "Get paginated list of notifications for current user")
    @GetMapping
    public ApiResponse<Page<NotificationResponse>> getNotifications(
            @RequestParam(required = false, defaultValue = "false") Boolean unreadOnly, Pageable pageable) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        Page<NotificationResponse> notifications =
                notificationService.getNotifications(currentUserId, pageable, unreadOnly);

        return ApiResponse.<Page<NotificationResponse>>builder()
                .data(notifications)
                .build();
    }

    @Operation(summary = "Get unread count", description = "Get count of unread notifications")
    @GetMapping(Endpoint.Notification.UNREAD_COUNT)
    public ApiResponse<Long> getUnreadCount() {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        Long count = notificationService.getUnreadCount(currentUserId);

        return ApiResponse.<Long>builder().data(count).build();
    }

    @Operation(summary = "Get notification by ID", description = "Get specific notification details")
    @GetMapping(Endpoint.Notification.ID)
    public ApiResponse<NotificationResponse> getNotificationById(@PathVariable UUID notificationId) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        NotificationResponse notification = notificationService.getById(notificationId, currentUserId);

        return ApiResponse.<NotificationResponse>builder().data(notification).build();
    }

    @Operation(summary = "Mark notification as read", description = "Mark a single notification as read")
    @PutMapping(Endpoint.Notification.MARK_READ)
    public ApiResponse<Void> markAsRead(@PathVariable UUID notificationId) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        notificationService.markAsRead(notificationId, currentUserId);

        return ApiResponse.<Void>builder()
                .message("Notification marked as read")
                .build();
    }

    @Operation(summary = "Mark all as read", description = "Mark all notifications as read for current user")
    @PutMapping(Endpoint.Notification.MARK_ALL_READ)
    public ApiResponse<Void> markAllAsRead() {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        notificationService.markAllAsRead(currentUserId);

        return ApiResponse.<Void>builder()
                .message("All notifications marked as read")
                .build();
    }

    @Operation(summary = "Toggle archive", description = "Archive or unarchive a notification")
    @PutMapping(Endpoint.Notification.ARCHIVE)
    public ApiResponse<Void> toggleArchive(
            @PathVariable UUID notificationId,
            @RequestParam(required = false, defaultValue = "true") Boolean archived) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        notificationService.toggleArchive(notificationId, currentUserId, archived);

        return ApiResponse.<Void>builder()
                .message(archived ? "Notification archived" : "Notification unarchived")
                .build();
    }

    @Operation(summary = "Delete notification", description = "Hard delete a notification (use archive for history)")
    @DeleteMapping(Endpoint.Notification.ID)
    public ApiResponse<Void> deleteNotification(@PathVariable UUID notificationId) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        notificationService.deleteNotification(notificationId, currentUserId);

        return ApiResponse.<Void>builder().message("Notification deleted").build();
    }
}
