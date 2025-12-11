package com.tripjoy.api.dto.response.feedback;

import com.tripjoy.api.dto.response.BaseResponse;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FeedbackResponse extends BaseResponse {
    UUID id;
    String content;
    Integer rating;
    String type;
    UUID userId;
}
