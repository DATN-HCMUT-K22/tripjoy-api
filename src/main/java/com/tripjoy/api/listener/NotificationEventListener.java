package com.tripjoy.api.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tripjoy.api.dto.event.NotificationEvent;
import com.tripjoy.api.entity.Notification;
import com.tripjoy.api.entity.User;
import com.tripjoy.api.exception.AppException;
import com.tripjoy.api.exception.ErrorCode;
import com.tripjoy.api.repository.NotificationRepository;
import com.tripjoy.api.repository.UserRepository;
import com.tripjoy.api.service.impl.SocketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Notification Event Listener
 * Handles notification creation and real-time broadcast after transaction
 * commits
 * 
 * Pattern: @Async + @TransactionalEventListener(AFTER_COMMIT)
 * Same pattern as GroupEventListener and MessageEventListener
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final SocketService socketService;
    private final ObjectMapper objectMapper;

    /**
     * Handle notification event
     * 1. Create notification in database
     * 2. Broadcast real-time notification via Socket.IO
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleNotificationEvent(NotificationEvent event) {
        try {
            log.info("EVENT: NotificationEvent received - type={}, recipient={}",
                    event.getType(), event.getRecipientId());

            // 1. VALIDATE & FETCH RECIPIENT
            User recipient = userRepository.findById(event.getRecipientId())
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

            // 2. FETCH ACTOR (optional)
            User actor = null;
            if (event.getActorId() != null) {
                actor = userRepository.findById(event.getActorId()).orElse(null);
                if (actor == null) {
                    log.warn("Actor not found: actorId={}", event.getActorId());
                }
            }

            // 3. CONVERT METADATA MAP TO JSON STRING
            String metadataJson = null;
            if (event.getMetadata() != null && !event.getMetadata().isEmpty()) {
                try {
                    metadataJson = objectMapper.writeValueAsString(event.getMetadata());
                } catch (Exception e) {
                    log.warn("Failed to serialize metadata to JSON: {}", e.getMessage());
                }
            }

            // 4. CREATE NOTIFICATION
            Notification notification = Notification.builder()
                    .recipient(recipient)
                    .actor(actor)
                    .type(event.getType())
                    .entityType(event.getEntityType())
                    .entityId(event.getEntityId())
                    .title(event.getTitle())
                    .message(event.getMessage())
                    .metadata(metadataJson)
                    .priority(event.getPriority())
                    .isRead(false)
                    .isArchived(false)
                    .build();

            Notification savedNotification = notificationRepository.save(notification);

            log.info("Notification created: id={}, type={}, recipient={}",
                    savedNotification.getId(),
                    savedNotification.getType(),
                    recipient.getUsername());

            // 5. BROADCAST REAL-TIME VIA SOCKET.IO
            try {
                socketService.sendNotification(
                        event.getRecipientId(),
                        savedNotification);
            } catch (Exception socketException) {
                // Log but don't fail the transaction if Socket.IO broadcast fails
                log.error("Failed to broadcast notification via Socket.IO: notificationId={}, error={}",
                        savedNotification.getId(),
                        socketException.getMessage());
            }

        } catch (Exception e) {
            log.error("CRITICAL ERROR handling NotificationEvent: type={}, recipient={}, error={}",
                    event.getType(),
                    event.getRecipientId(),
                    e.getMessage(),
                    e);
        }
    }
}
