package com.tripjoy.api.dto.request;

import jakarta.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(name = "UserStatusUpdateRequest", description = "Request payload for updating user status (Admin only)")
public class UserStatusUpdateRequest {

    @NotNull(message = "isLocked cannot be null")
    @Schema(
            name = "isLocked",
            description = "Status indicating whether the user is locked out",
            type = "boolean",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "true")
    Boolean isLocked;
}
