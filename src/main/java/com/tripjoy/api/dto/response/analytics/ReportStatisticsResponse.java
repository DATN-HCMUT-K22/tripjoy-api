package com.tripjoy.api.dto.response.analytics;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReportStatisticsResponse {

    @JsonProperty("total_reports")
    Long totalReports;

    @JsonProperty("pending_reports")
    Long pendingReports;

    @JsonProperty("processed_reports")
    Long processedReports;

    @JsonProperty("dismissed_reports")
    Long dismissedReports;

    @JsonProperty("by_type")
    Map<String, Long> byType; // SPAM: 50, HARASSMENT: 30, etc.

    @JsonProperty("by_content_type")
    Map<String, Long> byContentType; // POST: 80, COMMENT: 40

    @JsonProperty("avg_handling_time_hours")
    Double avgHandlingTimeHours;

    @JsonProperty("trend")
    List<DailyTrendDto> trend;
}
