package com.tripjoy.api.dto.response.report;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tripjoy.api.dto.response.simple.UserSimpleResponse;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class HandleReportResponse {

    UUID id; // ID from Handle_report_content

    @JsonProperty("report_content_id")
    String reportContentId;

    @JsonProperty("handled_by")
    UserSimpleResponse handledBy; // System admin or business admin who handled it

    @JsonProperty("handled_at")
    LocalDateTime handledAt;

    String status;
    String description;

    @JsonProperty("moderation_action")
    ModerationActionResponse moderationAction;
}
