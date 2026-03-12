package com.tripjoy.api.entity;

import jakarta.persistence.*;

import com.tripjoy.api.enums.ActivityAction;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "activity_logs",
        indexes = {
            @Index(name = "idx_user_action", columnList = "user_id, action, created_at"),
            @Index(name = "idx_activity_entity", columnList = "entity_type, entity_id"),
            @Index(name = "idx_created_at", columnList = "created_at")
        })
public class ActivityLog extends BaseEntity {

    // USER - Who performed this action
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // ACTION - What action was performed
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ActivityAction action;

    // ENTITY REFERENCE - What entity was affected
    @Column(name = "entity_type", length = 50)
    private String entityType; // "POST", "COMMENT", "GROUP", "NOTIFICATION", etc.

    @Column(name = "entity_id")
    private String entityId;

    // METADATA - Additional context (JSON string)
    @Column(columnDefinition = "TEXT")
    private String metadata;

    // REQUEST INFO
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    // No inverse relationship from User to ActivityLog
    // This keeps User domain clean and ActivityLog as pure audit trail
}
