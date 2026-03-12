package com.tripjoy.api.dto.request.feedback;

import jakarta.validation.constraints.NotBlank;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FeedbackRequest {

    @NotBlank
    @Schema(
            description = "Type of feedback (e.g., BUG_REPORT, SUGGESTION)",
            example = "BUG_REPORT",
            requiredMode = Schema.RequiredMode.REQUIRED)
    String type; // maps to column "type"

    @NotBlank
    @Schema(
            description = "Summary title of the feedback",
            example = "App crash on startup",
            requiredMode = Schema.RequiredMode.REQUIRED)
    String title;

    @NotBlank
    @Schema(
            description = "Detailed content of the feedback",
            example = "The app crashes when opening the map.",
            requiredMode = Schema.RequiredMode.REQUIRED)
    String content; // maps to column "content"
}
