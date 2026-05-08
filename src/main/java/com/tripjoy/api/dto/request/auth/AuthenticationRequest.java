package com.tripjoy.api.dto.request.auth;

import jakarta.validation.constraints.NotBlank;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthenticationRequest {

    @NotBlank(message = "INVALID_REQUEST")
    @Schema(
            name = "username",
            description = "Username for login",
            type = "String",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "user")
    String username;

    @NotBlank(message = "INVALID_REQUEST")
    @Schema(
            name = "password",
            description = "Users's password",
            type = "String",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "StrongP@ss123")
    String password;
}
