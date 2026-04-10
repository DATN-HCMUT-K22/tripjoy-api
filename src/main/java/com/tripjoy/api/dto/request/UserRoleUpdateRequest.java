package com.tripjoy.api.dto.request;

import java.util.Set;

import jakarta.validation.constraints.NotEmpty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(name = "UserRoleUpdateRequest", description = "Request payload for updating user roles (Admin only)")
public class UserRoleUpdateRequest {

    @NotEmpty(message = "Roles cannot be empty")
    @Schema(
            name = "roles",
            description = "Set of role names",
            type = "array",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "[\"USER\", \"ADMIN\"]")
    Set<String> roles;
}
