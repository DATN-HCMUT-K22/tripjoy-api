package com.tripjoy.api.dto.request;

import java.util.Set;

import jakarta.validation.constraints.NotEmpty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(name = "AdminUserCreationRequest", description = "System admin request payload for creating a user with roles")
public class AdminUserCreationRequest extends UserCreationRequest {

    @NotEmpty(message = "Roles cannot be empty")
    @Schema(
            name = "roles",
            description = "Set of role names to assign to the new user",
            type = "array",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "[\"USER\"]")
    Set<String> roles;
}
