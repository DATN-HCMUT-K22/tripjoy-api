package com.tripjoy.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthenticationRequest {

    @NotBlank(message = "{not_blank}")
    @Schema(
            name = "username",
            description = "Username for login",
            type = "String",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "johndoe"
    )
    String username;

    @NotBlank(message = "{not_blank}")
    @Schema(
            name = "password",
            description = "User's password",
            type = "String",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "P@sswd123."
    )
    String password;
}