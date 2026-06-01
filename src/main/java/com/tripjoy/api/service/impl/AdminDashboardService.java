package com.tripjoy.api.service.impl;

import java.time.Instant;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tripjoy.api.configuration.redis.RedisCacheConfig;
import com.tripjoy.api.dto.projection.AdminDashboardOverviewProjection;
import com.tripjoy.api.dto.response.dashboard.AdminDashboardOverviewResponse;
import com.tripjoy.api.repository.AdminDashboardRepository;
import com.tripjoy.api.service.IAdminDashboardService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminDashboardService implements IAdminDashboardService {

    AdminDashboardRepository adminDashboardRepository;

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = RedisCacheConfig.CACHE_ADMIN_DASHBOARD_OVERVIEW, key = "'v1'", sync = true)
    public AdminDashboardOverviewResponse getOverview() {
        AdminDashboardOverviewProjection overview = adminDashboardRepository.getOverview();

        return AdminDashboardOverviewResponse.builder()
                .users(AdminDashboardOverviewResponse.UserMetrics.builder()
                        .total(overview.getTotalUsers())
                        .locked(overview.getLockedUsers())
                        .build())
                .content(AdminDashboardOverviewResponse.ContentMetrics.builder()
                        .posts(overview.getTotalPosts())
                        .comments(overview.getTotalComments())
                        .itineraries(overview.getTotalItineraries())
                        .groups(overview.getTotalGroups())
                        .build())
                .moderation(AdminDashboardOverviewResponse.ModerationMetrics.builder()
                        .pendingReports(overview.getPendingReports())
                        .processedReports(overview.getProcessedReports())
                        .dismissedReports(overview.getDismissedReports())
                        .totalActions(overview.getTotalModerationActions())
                        .build())
                .generatedAt(Instant.now())
                .build();
    }
}
