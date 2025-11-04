package com.tripjoy.api.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tripjoy.api.dto.response.simple.UserSimpleResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FeedbackRequest {

    @NotBlank
    @Schema(description = "Type of feedback (e.g., BUG_REPORT, SUGGESTION)",
            example = "BUG_REPORT", requiredMode = Schema.RequiredMode.REQUIRED)
    String type; // maps to column "type"

    @NotBlank
    @Schema(description = "Detailed content of the feedback",
            example = "The app crashes when opening the map.", requiredMode = Schema.RequiredMode.REQUIRED)
    String content; // maps to column "content"
}