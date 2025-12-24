package com.tripjoy.api.dto.event;

import com.tripjoy.api.enums.NotificationType;
import lombok.*;

import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEvent {

    // Recipient - User who will receive this notification (REQUIRED)
    private UUID recipientId;

    // Actor - User who triggered this notification (OPTIONAL)
    // NULL for system notifications
    private UUID actorId;

    // Notification type (REQUIRED)
    private NotificationType type;

    // Entity reference - Polymorphic reference to related entity
    private String entityType; // "POST", "COMMENT", "GROUP", "ITINERARY"
    private String entityId; // UUID as string

    // Content
    private String title;
    private String message;

    // Extra metadata (OPTIONAL)
    private Map<String, Object> metadata;

    // Priority (OPTIONAL)
    private String priority; // "HIGH", "NORMAL", "LOW"
}
