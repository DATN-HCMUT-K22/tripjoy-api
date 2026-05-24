package com.tripjoy.api.dto.response.feedback;

import java.util.UUID;

import com.tripjoy.api.dto.response.BaseResponse;

import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FeedbackResponse extends BaseResponse {
    UUID id;
    String title;
    String content;
    Integer rating;
    String type;
    String status;
    UUID userId;

    UUID receiverId;

    UUID parentFeedbackId;

    UUID reportContentId;
}
