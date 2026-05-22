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
public class UserStatisticsResponse {

    @JsonProperty("total_users")
    Long totalUsers;

    @JsonProperty("active_users")
    Long activeUsers;

    @JsonProperty("locked_users")
    Long lockedUsers;

    @JsonProperty("deleted_users")
    Long deletedUsers;

    @JsonProperty("new_users_this_month")
    Long newUsersThisMonth;

    @JsonProperty("growth_rate_percent")
    Double growthRatePercent;

    @JsonProperty("by_role")
    Map<String, Long> byRole; // USER: 5200, ADMIN: 20

    @JsonProperty("top_reporters")
    List<TopReporterDto> topReporters;
}
