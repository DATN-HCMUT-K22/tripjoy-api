package com.tripjoy.api.dto.response.report;

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
public class ReportContentSimpleResponse {
    UUID id;
    
    @JsonProperty("content_type")
    String contentType;
    
    @JsonProperty("reported_entity_id")
    UUID reportedEntityId;
    
    @JsonProperty("report_type")
    String reportType;
    
    String status;
    
    @JsonProperty("reported_content_text")
    String reportedContentText;
    
    @JsonProperty("reported_media_url")
    String reportedMediaUrl;
    
    UserSimpleResponse reporter;
    
    @JsonProperty("reported_user")
    UserSimpleResponse reportedUser;
}
