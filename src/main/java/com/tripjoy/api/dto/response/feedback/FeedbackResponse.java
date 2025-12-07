package com.tripjoy.api.dto.response.feedback;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tripjoy.api.dto.response.simple.UserSimpleResponse;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FeedbackResponse {

    UUID id; // Từ bảng "Feedback"
    String type; // Từ bảng "Feedback"
    String content; // Từ bảng "Feedback"
    String status; // Từ bảng "Feedback"

    @JsonProperty("created_at")
    LocalDateTime createdAt; // Từ bảng "Feedback"

    @JsonProperty("created_by")
    UserSimpleResponse createdBy; // Join từ "created_by" (uuid)
}
