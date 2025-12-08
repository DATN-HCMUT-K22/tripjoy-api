package com.tripjoy.api.entity;

import com.tripjoy.api.configuration.security.UserDetailsCustom;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor
@MappedSuperclass
@SuperBuilder
@Getter
@Setter
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
        UserDetailsCustom user = UserDetailsCustom.getCurrentUser();
        if (user != null) {
            if (this.createdBy == null) this.createdBy = user.getUserId();
            this.updatedBy = user.getUserId();
        }
    }

    @PreUpdate
    public void onPreUpdate() {
        this.updatedAt = LocalDateTime.now();
        UserDetailsCustom user = UserDetailsCustom.getCurrentUser();
        if (user != null) {
            this.updatedBy = user.getUserId();
        }
    }
}
