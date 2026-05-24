package com.tripjoy.api.dto.response.report;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tripjoy.api.dto.response.BaseResponse;
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
public class ReportResponse extends BaseResponse {
    UUID id;
    String reason;
    String status;

    String description;

    UUID reportedBy;

    UserSimpleResponse reporter;

    @JsonProperty("reported_user")
    UserSimpleResponse reportedUser;

    UUID reportedEntityId;
    String reportedEntityType;

    @JsonProperty("reported_content_text")
    String reportedContentText;

    @JsonProperty("reported_media_url")
    String reportedMediaUrl;
}
