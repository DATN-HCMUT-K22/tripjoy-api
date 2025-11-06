package com.tripjoy.api.dto.response.report;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tripjoy.api.dto.response.simple.UserSimpleResponse;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReportResponse {

    String id; // ID của bảng "Report_to"

    @JsonProperty("report_type")
    String reportType;

    String status;
    String description;

    @JsonProperty("content_id")
    String contentId; // ID của "Report_content" (để admin truy cập)

    @JsonProperty("reported_by")
    UserSimpleResponse reportedBy; // Join từ "user_id"

}
