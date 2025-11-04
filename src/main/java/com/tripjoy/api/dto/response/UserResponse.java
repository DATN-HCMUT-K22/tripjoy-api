package com.tripjoy.api.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserResponse {
    String id;
    String username;
    String email;
    boolean isEmailVerified;
    String phoneNumber;
    String fullName;
    String bio;
    String avatarUrl;

    LocalDateTime createdAt;
    LocalDateTime updatedAt;

    boolean isDeleted;
    boolean isLocked;

    Set<RoleResponse> roles;
}
