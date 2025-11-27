package com.tripjoy.api.dto.response.feedback;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tripjoy.api.dto.response.simple.UserSimpleResponse;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FeedbackResponseResponse {

    String id; // ID from Response_feedback table

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