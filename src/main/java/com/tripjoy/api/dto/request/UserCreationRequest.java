package com.tripjoy.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserCreationRequest {

    @NotBlank(message = "{not_blank}")
    @Schema(
            name = "username",
            description = "Username (unique)",
            type = "String",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "johndoe"
    )
    String username;

    @NotBlank(message = "{not_blank}")
    @Email(message = "{invalid_email}")
    @Schema(
            name = "email",
            description = "Email address (unique)",
            type = "String",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "johndoe@example.com"
    )
    String email;

    @NotBlank(message = "{not_blank}")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Schema(
            name = "password",
            description = "Password (minimum 8 characters)",
            type = "String",
            format = "password", // Helps hide value in Swagger UI
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "StrongP@ss123"
    )
    String password;

    @Schema(
            name = "fullName",
            description = "Users's full name",
            type = "String",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            example = "John Doe"
    )
    String fullName;
}