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
public class SystemHealthResponse {

    @JsonProperty("api_uptime_percent")
    Double apiUptimePercent;

    @JsonProperty("avg_response_time_ms")
    Integer avgResponseTimeMs;

    @JsonProperty("error_rate_percent")
    Double errorRatePercent;

    @JsonProperty("db_connection_pool")
    Map<String, Object> dbConnectionPool;

    @JsonProperty("recent_errors")
    List<RecentErrorDto> recentErrors;

    @JsonProperty("cache_hit_rate_percent")
    Double cacheHitRatePercent;
}
