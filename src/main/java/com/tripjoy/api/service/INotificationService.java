package com.tripjoy.api.service;

import com.tripjoy.api.dto.response.NotificationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * Notification Service Interface
 */
public interface INotificationService {

    Page<NotificationResponse> getNotifications(UUID userId, Pageable pageable, Boolean unreadOnly);

    Long getUnreadCount(UUID userId);

    void markAsRead(UUID notificationId, UUID userId);

    void markAllAsRead(UUID userId);

    void toggleArchive(UUID notificationId, UUID userId, boolean archived);

    void deleteNotification(UUID notificationId, UUID userId);

    NotificationResponse getById(UUID notificationId, UUID userId);
}
