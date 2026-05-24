package com.tripjoy.api.dto.request.report;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ModerationActionRequest {

    @NotNull(message = "User ID is required")
    @JsonProperty("user_id")
    @Schema(
            name = "userId",
            description = "The unique ID of the users receiving the action",
            type = "String",
            requiredMode = Schema.RequiredMode.REQUIRED)
    UUID userId;

    @NotBlank
    @Schema(
            name = "actionType",
            description = "The type of moderation action",
            type = "String",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "BAN_USER" // e.g., BAN_USER, WARN_USER, DELETE_POST
            )
    String actionType;

    @Schema(
            name = "note",
            description = "Admin's justification or notes for this action",
            type = "String",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            example = "Users violated community guidelines multiple times.")
    String note;
}
