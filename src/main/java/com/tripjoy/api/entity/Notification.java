package com.tripjoy.api.entity;

import com.tripjoy.api.enums.NotificationType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "notifications", indexes = {
        @Index(name = "idx_recipient_unread", columnList = "recipient_id, is_read, created_at"),
        @Index(name = "idx_recipient_created", columnList = "recipient_id, created_at"),
        @Index(name = "idx_entity", columnList = "entity_type, entity_id"),
        @Index(name = "idx_actor", columnList = "actor_id, created_at")
})
public class Notification extends BaseEntity {

    // RECIPIENT - Người nhận notification (REQUIRED)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;

    // ACTOR - Người gây ra notification (OPTIONAL)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id")
    private User actor;

    // NOTIFICATION TYPE
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private NotificationType type;

    // POLYMORPHIC REFERENCE - Entity được reference
    @Column(name = "entity_type", length = 50)
    private String entityType; // "POST", "COMMENT", "GROUP", "ITINERARY", "CHAT_MESSAGE"

    @Column(name = "entity_id")
    private String entityId; // UUID as String for flexibility

    // CONTENT
    @Column(length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String message;

    // METADATA - Extra data stored as JSON string
    @Column(columnDefinition = "TEXT")
    private String metadata;
    // Store JSON string: "{\"postPreview\": \"Amazing trip...\", \"commentText\":
    // \"Great!\"}"

    // USER-SPECIFIC STATE (của recipient)
    @Builder.Default
    @Column(nullable = false)
    private Boolean isRead = false;

    private LocalDateTime readAt;

    @Builder.Default
    @Column(nullable = false)
    private Boolean isArchived = false;

    // PRIORITY (optional)
    @Column(length = 20)
    private String priority; // "HIGH", "NORMAL", "LOW"
}
