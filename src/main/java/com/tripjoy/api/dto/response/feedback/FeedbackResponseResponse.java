package com.tripjoy.api.dto.response.feedback;

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
public class FeedbackResponseResponse {

    UUID id; // ID from Response_feedback table

    @JsonProperty("feedback_id")
    String feedbackId;

    @JsonProperty("responded_by")
    UserSimpleResponse respondedBy; // Admin/BA

    @JsonProperty("response_for")
    UserSimpleResponse responseFor; // Users who sent feedback

    @JsonProperty("created_at")
    LocalDateTime createdAt;

    String description;
    String status;
}
