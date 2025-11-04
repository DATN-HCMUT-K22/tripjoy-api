package com.tripjoy.api.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReportRequest {

    @NotBlank(message = "{not_blank}")
    @JsonProperty("content_id")
    @Schema(description = "ID of the reported content (Post ID, Comment ID, User ID, etc.)",
            example = "a1b2c3d4-...", requiredMode = Schema.RequiredMode.REQUIRED)
    String contentId;

    @NotBlank(message = "{not_blank}")
    @JsonProperty("content_type")
    @Schema(description = "Type of the reported content",
            example = "POST", requiredMode = Schema.RequiredMode.REQUIRED)
    String contentType; // "POST", "COMMENT", "USER" (to let service know which table to join)

    @NotBlank(message = "{not_blank}")
    @JsonProperty("report_type")
    @Schema(description = "Reason for reporting (from the Report_to table)",
            example = "SPAM", requiredMode = Schema.RequiredMode.REQUIRED)
    String reportType; // maps to "report_type"

    @Schema(description = "Additional description (from the Report_to table)",
            example = "This account posts phishing links.")
    String description; // maps to "description"
}