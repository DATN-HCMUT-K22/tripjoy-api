package com.tripjoy.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(name = "ChangePasswordRequest", description = "Request payload for changing user password")
public class ChangePasswordRequest {

    @NotBlank(message = "Old password is required")
    @Schema(
            name = "oldPassword",
            description = "Current password",
            type = "String",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "OldStr0ngP@ss")
    String oldPassword;

    @NotBlank(message = "New password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    @Schema(
            name = "newPassword",
            description = "New password",
            type = "String",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "NewStr0ngP@ss")
    String newPassword;

    @NotBlank(message = "Confirm password is required")
    @Schema(
            name = "confirmPassword",
            description = "Confirm new password",
            type = "String",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "NewStr0ngP@ss")
    String confirmPassword;
}
