package com.tripjoy.api.dto.response.feedback;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tripjoy.api.dto.response.BaseResponse;
import com.tripjoy.api.dto.response.report.ReportContentSimpleResponse;
import com.tripjoy.api.dto.response.simple.UserSimpleResponse;

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
    UserSimpleResponse sender;

    UUID receiverId;
    UserSimpleResponse receiver;

    @JsonProperty("parent_feedback")
    ParentFeedbackSimpleResponse parentFeedback;

    ReportContentSimpleResponse report;
}
