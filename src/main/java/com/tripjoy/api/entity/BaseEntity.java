package com.tripjoy.api.entity;

import com.tripjoy.api.utils.SecurityUtils;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor
@MappedSuperclass
@SuperBuilder
@Getter
@Setter
@Slf4j
public abstract class BaseEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private LocalDateTime createdAt;
    private UUID createdBy;
    private LocalDateTime updatedAt;
    private UUID updatedBy;

    @PrePersist
    public void onPrePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;

        // Get current user ID safely (returns null if not authenticated)
        UUID currentUserId = SecurityUtils.getCurrentUserIdSafe();

        if (currentUserId != null) {
            if (this.createdBy == null) {
                this.createdBy = currentUserId;
            }
            this.updatedBy = currentUserId;
        } else {
            log.warn("PrePersist: Cannot get current user - createdBy/updatedBy will be null for entity: {}",
                    this.getClass().getSimpleName());
        }
    }

    @PreUpdate
    public void onPreUpdate() {
        this.updatedAt = LocalDateTime.now();

        UUID currentUserId = SecurityUtils.getCurrentUserIdSafe();

        if (currentUserId != null) {
            this.updatedBy = currentUserId;
        } else {
            log.warn("PreUpdate: Cannot get current user - updatedBy will be null for entity: {}",
                    this.getClass().getSimpleName());
        }
    }
}
