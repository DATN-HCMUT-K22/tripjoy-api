package com.tripjoy.api.dto.response.dashboard;

import java.time.Instant;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AdminDashboardOverviewResponse {

    UserMetrics users;
    ContentMetrics content;
    ModerationMetrics moderation;
    Instant generatedAt;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class UserMetrics {
        long total;
        long locked;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class ContentMetrics {
        long posts;
        long comments;
        long itineraries;
        long groups;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class ModerationMetrics {
        long pendingReports;
        long processedReports;
        long dismissedReports;
        long totalActions;
    }
}
