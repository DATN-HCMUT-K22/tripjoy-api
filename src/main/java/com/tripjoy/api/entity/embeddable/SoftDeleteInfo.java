package com.tripjoy.api.entity.embeddable;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import lombok.Getter;
import lombok.Setter;

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

    @Column(name = "is_deleted", nullable = false, columnDefinition = "boolean default false")
    private Boolean isDeleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by")
    private String deletedBy;

    public void markAsDeleted(String deletedBy) {
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
        this.deletedBy = deletedBy;
    }

    public void restore() {
        this.isDeleted = false;
        this.deletedAt = null;
        this.deletedBy = null;
    }

    public boolean isDeleted() {
        return Boolean.TRUE.equals(isDeleted);
    }
}
