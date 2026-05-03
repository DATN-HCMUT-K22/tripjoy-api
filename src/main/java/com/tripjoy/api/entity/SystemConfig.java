package com.tripjoy.api.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;

import com.tripjoy.api.utils.SecurityUtils;

import lombok.*;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "system_config")
public class SystemConfig {

    @Id
    @Column(name = "config_key", length = 100)
    private String configKey;

    @Column(name = "config_value", nullable = false, columnDefinition = "TEXT")
    private String configValue;

    @Column(name = "data_type", nullable = false, length = 20)
    private String dataType;

    @Column(name = "config_group", nullable = false, length = 50)
    private String configGroup;

    @Column(columnDefinition = "TEXT")
    private String description;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String updatedBy;

    @PrePersist
    public void onPrePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
        this.updatedBy = SecurityUtils.getCurrentUserIdSafe() != null 
                ? SecurityUtils.getCurrentUserIdSafe().toString() 
                : "SYSTEM";
    }

    @PreUpdate
    public void onPreUpdate() {
        this.updatedAt = LocalDateTime.now();
        this.updatedBy = SecurityUtils.getCurrentUserIdSafe() != null 
                ? SecurityUtils.getCurrentUserIdSafe().toString() 
                : "SYSTEM";
    }
}
