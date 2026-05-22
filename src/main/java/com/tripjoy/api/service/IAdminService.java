package com.tripjoy.api.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.tripjoy.api.dto.request.report.ModerationActionRequest;
import com.tripjoy.api.dto.response.analytics.ContentStatisticsResponse;
import com.tripjoy.api.dto.response.analytics.ReportStatisticsResponse;
import com.tripjoy.api.dto.response.analytics.SystemHealthResponse;
import com.tripjoy.api.dto.response.analytics.UserStatisticsResponse;
import com.tripjoy.api.dto.response.report.ModerationActionResponse;

public interface IAdminService {

    /**
     * Perform moderation action on a user (BAN, WARN, SUSPEND)
     */
    ModerationActionResponse moderateUser(ModerationActionRequest request, UUID adminId);

    /**
     * Get all moderation actions with optional filters (admin only)
     */
    Page<ModerationActionResponse> getModerationActions(
            UUID userId,
            String actionType,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable);

    /**
     * Get moderation history for a specific user (admin only)
     */
    List<ModerationActionResponse> getUserModerationHistory(UUID userId);

    /**
     * Get report statistics (admin dashboard)
     *
     * @param startDate Optional start date for filtering
     * @param endDate Optional end date for filtering
     * @return ReportStatisticsResponse with aggregated data
     */
    ReportStatisticsResponse getReportStatistics(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Get user statistics (admin dashboard)
     *
     * @return UserStatisticsResponse with aggregated data
     */
    UserStatisticsResponse getUserStatistics();

    /**
     * Get content statistics (admin dashboard)
     *
     * @param startDate Optional start date for filtering
     * @param endDate Optional end date for filtering
     * @return ContentStatisticsResponse with aggregated data
     */
    ContentStatisticsResponse getContentStatistics(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Get system health metrics (admin dashboard)
     *
     * @return SystemHealthResponse with system metrics
     */
    SystemHealthResponse getSystemHealth();
}
