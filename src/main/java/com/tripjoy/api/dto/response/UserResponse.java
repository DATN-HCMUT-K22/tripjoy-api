package com.tripjoy.api.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserResponse {
    UUID id;
    String username;
    String email;
    LocalDateTime dateOfBirth;
    boolean isEmailVerified;
    String phoneNumber;
    String fullName;
    String bio;
    String avatarUrl;

    Long credits;

    LocalDateTime createdAt;
    LocalDateTime updatedAt;

    boolean isDeleted;
    boolean isLocked;

    Set<RoleResponse> roles;
}
