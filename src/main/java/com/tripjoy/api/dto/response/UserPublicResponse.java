package com.tripjoy.api.dto.response;

import java.time.LocalDate;
import java.util.UUID;

import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * Public profile — safe to expose to any authenticated user.
 * Intentionally omits sensitive fields: email, phone, credits, isLocked, roles.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserPublicResponse {
    UUID id;
    String username;
    String fullName;
    String bio;
    String avatarUrl;
    LocalDate dateOfBirth;
    boolean isEmailVerified;
}
