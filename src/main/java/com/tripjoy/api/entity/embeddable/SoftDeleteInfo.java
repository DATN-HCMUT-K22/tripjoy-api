package com.tripjoy.api.entity.embeddable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Embeddable component for soft delete functionality.
 * Following enterprise pattern used in Spring Data Envers and Hibernate.
 * 
 * Usage:
 * 
 * <pre>
 * @Embedded
 * private SoftDeleteInfo softDeleteInfo = new SoftDeleteInfo();
 * </pre>
 */
@Embeddable
@Getter
@Setter
public class SoftDeleteInfo {

    /**
     * Soft delete flag
     */
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    /**
     * Timestamp when entity was soft deleted
     */
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    /**
     * User ID who performed the soft delete
     */
    @Column(name = "deleted_by")
    private String deletedBy;

    /**
     * Mark entity as deleted
     * 
     * @param deletedBy User ID performing the deletion
     */
    public void markAsDeleted(String deletedBy) {
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
        this.deletedBy = deletedBy;
    }

    /**
     * Restore soft deleted entity
     */
    public void restore() {
        this.isDeleted = false;
        this.deletedAt = null;
        this.deletedBy = null;
    }

    /**
     * Check if entity is deleted
     */
    public boolean isDeleted() {
        return Boolean.TRUE.equals(isDeleted);
    }
}
