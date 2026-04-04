package com.tripjoy.api.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserResponse extends BaseResponse {
    UUID id;
    String username;
    String email;
    LocalDate dateOfBirth;
    boolean isEmailVerified;
    String phoneNumber;
    String fullName;
    String bio;
    String avatarUrl;

    Long credits;

    boolean isDeleted;
    boolean isLocked;

    Set<RoleResponse> roles;
}
