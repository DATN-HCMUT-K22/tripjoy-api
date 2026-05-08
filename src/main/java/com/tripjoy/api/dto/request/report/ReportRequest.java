package com.tripjoy.api.dto.request.report;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReportRequest {

    @NotNull(message = "Content ID is required")
    @JsonProperty("content_id")
    @Schema(
            description = "ID of the reported content (Post ID, Comment ID, Users ID, etc.)",
            example = "a1b2c3d4-...",
            requiredMode = Schema.RequiredMode.REQUIRED)
    UUID contentId;

    @NotBlank(message = "INVALID_REQUEST")
    @JsonProperty("content_type")
    @Schema(description = "Type of the reported content", example = "POST", requiredMode = Schema.RequiredMode.REQUIRED)
    String contentType; // "POST", "COMMENT", "USER" (to let service know which table to join)

    @NotBlank(message = "INVALID_REQUEST")
    @JsonProperty("report_type")
    @Schema(
            description = "Reason for reporting (from the Report_to table)",
            example = "SPAM",
            requiredMode = Schema.RequiredMode.REQUIRED)
    String reportType; // maps to "report_type"

    @Schema(
            description = "Additional description (from the Report_to table)",
            example = "This account posts phishing links.")
    String description; // maps to "description"
}
