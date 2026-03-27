package com.tripjoy.api.service.impl;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tripjoy.api.dto.response.NotificationResponse;
import com.tripjoy.api.entity.Notification;
import com.tripjoy.api.exception.AppException;
import com.tripjoy.api.exception.ErrorCode;
import com.tripjoy.api.mapper.NotificationMapper;
import com.tripjoy.api.repository.NotificationRepository;
import com.tripjoy.api.service.INotificationService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NotificationService implements INotificationService {

    NotificationRepository notificationRepository;
    NotificationMapper notificationMapper;

    // Get notifications for a user with optional unread filter
    @Override
    @Transactional(readOnly = true)
    public Page<NotificationResponse> getNotifications(UUID userId, Pageable pageable, Boolean unreadOnly) {
        Page<Notification> notifications;

        if (Boolean.TRUE.equals(unreadOnly)) {
            notifications = notificationRepository.findUnreadByRecipient(userId, pageable);
        } else {
            notifications = notificationRepository.findByRecipient(userId, pageable);
        }

        return notifications.map(notificationMapper::toResponse);
    }

    // Get unread notification count
    @Override
    @Transactional(readOnly = true)
    public Long getUnreadCount(UUID userId) {
        return notificationRepository.countUnreadByRecipient(userId);
    }

    // Mark notification as read
    @Override
    @Transactional
    public void markAsRead(UUID notificationId, UUID userId) {
        Notification notification = notificationRepository
                .findByIdAndRecipient(notificationId, userId)
                .orElseThrow(() -> new AppException(ErrorCode.NOTIF_NOT_FOUND));

        if (Boolean.FALSE.equals(notification.getIsRead())) {
            LocalDateTime now = LocalDateTime.now();
            int updated = notificationRepository.markAsRead(notificationId, userId, now);

            if (updated > 0) {
                log.info("Notification marked as read: notificationId={}, userId={}", notificationId, userId);
            }
        }
    }

    // Mark all notifications as read for a user
    @Override
    @Transactional
    public void markAllAsRead(UUID userId) {
        LocalDateTime now = LocalDateTime.now();
        int updated = notificationRepository.markAllAsRead(userId, now);
        log.info("Marked all notifications as read for user: userId={}, count={}", userId, updated);
    }

    // Toggle archive status
    @Override
    @Transactional
    public void toggleArchive(UUID notificationId, UUID userId, boolean archived) {
        Notification notification = notificationRepository
                .findByIdAndRecipient(notificationId, userId)
                .orElseThrow(() -> new AppException(ErrorCode.NOTIF_NOT_FOUND));

        LocalDateTime now = LocalDateTime.now();
        int updated = notificationRepository.updateArchived(notificationId, userId, archived, now);

        if (updated > 0) {
            log.info(
                    "Notification archive status updated: notificationId={}, userId={}, archived={}",
                    notificationId,
                    userId,
                    archived);
        }
    }

    // Hard delete notification
    @Override
    @Transactional
    public void deleteNotification(UUID notificationId, UUID userId) {
        Notification notification = notificationRepository
                .findByIdAndRecipient(notificationId, userId)
                .orElseThrow(() -> new AppException(ErrorCode.NOTIF_NOT_FOUND));

        notificationRepository.delete(notification);
        log.info("Notification deleted: notificationId={}, userId={}", notificationId, userId);
    }

    // Get notification by ID with ownership validation
    @Override
    @Transactional(readOnly = true)
    public NotificationResponse getById(UUID notificationId, UUID userId) {
        Notification notification = notificationRepository
                .findByIdAndRecipient(notificationId, userId)
                .orElseThrow(() -> new AppException(ErrorCode.NOTIF_NOT_FOUND));

        return notificationMapper.toResponse(notification);
    }
}
