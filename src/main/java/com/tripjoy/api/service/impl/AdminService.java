package com.tripjoy.api.service.impl;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tripjoy.api.constant.ModerationActionType;
import com.tripjoy.api.constant.ReportType;
import com.tripjoy.api.dto.request.report.ModerationActionRequest;
import com.tripjoy.api.dto.response.analytics.ContentStatisticsResponse;
import com.tripjoy.api.dto.response.analytics.DailyTrendDto;
import com.tripjoy.api.dto.response.analytics.MostActiveUserDto;
import com.tripjoy.api.dto.response.analytics.ReportStatisticsResponse;
import com.tripjoy.api.dto.response.analytics.SystemHealthResponse;
import com.tripjoy.api.dto.response.analytics.TopReporterDto;
import com.tripjoy.api.dto.response.analytics.UserStatisticsResponse;
import com.tripjoy.api.dto.response.report.ModerationActionResponse;
import com.tripjoy.api.entity.ModerationAction;
import com.tripjoy.api.entity.User;
import com.tripjoy.api.exception.AppException;
import com.tripjoy.api.exception.ErrorCode;
import com.tripjoy.api.mapper.ModerationMapper;
import com.tripjoy.api.repository.CommentRepository;
import com.tripjoy.api.repository.HandleReportContentRepository;
import com.tripjoy.api.repository.ModerationActionRepository;
import com.tripjoy.api.repository.PostRepository;
import com.tripjoy.api.repository.ReportContentRepository;
import com.tripjoy.api.repository.UserRepository;
import com.tripjoy.api.service.IAdminService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AdminService implements IAdminService {

    ModerationActionRepository moderationActionRepository;
    UserRepository userRepository;
    ReportContentRepository reportContentRepository;
    HandleReportContentRepository handleReportContentRepository;
    PostRepository postRepository;
    CommentRepository commentRepository;
    ModerationMapper moderationMapper;

    @Override
    @Transactional
    public ModerationActionResponse moderateUser(ModerationActionRequest request, UUID adminId) {
        log.info("Admin {} moderating user {} with action {}", adminId, request.getUserId(), request.getActionType());

        // 1. Validate action type
        if (!ModerationActionType.isValid(request.getActionType())) {
            throw new AppException(ErrorCode.INVALID_REQUEST,
                    "Invalid moderation action type: " + request.getActionType());
        }

        ModerationActionType actionType = ModerationActionType.fromString(request.getActionType());

        // 2. Fetch target user
        User targetUser = userRepository.findById(UUID.fromString(request.getUserId()))
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND,
                                                    "User not found: " + request.getUserId()));

        // 3. Fetch admin user
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND,
                                                    "Admin not found: " + adminId));

        // 4. Update user status based on action type
        if (actionType.shouldLockAccount()) {
            targetUser.setIsLocked(true);
            log.info("User {} account locked due to {}", targetUser.getId(), actionType);
        } else {
            // WARN_USER doesn't lock account, just records warning
            log.info("User {} warned, account remains active", targetUser.getId());
        }
        userRepository.save(targetUser);

        // 5. Create moderation action record
        ModerationAction action = ModerationAction.builder()
                .actionType(actionType.name())
                .note(request.getNote())
                .user(targetUser)
                .ba(admin)
                .build();

        ModerationAction savedAction = moderationActionRepository.save(action);

        log.info("Moderation action {} created successfully", savedAction.getId());

        // 6. TODO: Send notification to user (email/push)
        // sendModerationNotification(targetUser, actionType, request.getNote());

        // 7. Map to response
        return moderationMapper.toModerationActionResponse(savedAction);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ModerationActionResponse> getModerationActions(
            UUID userId,
            String actionType,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable) {

        log.info("Fetching moderation actions with filters - userId: {}, actionType: {}", userId, actionType);

        // Validate action type if provided
        if (actionType != null && !ModerationActionType.isValid(actionType)) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Invalid action type: " + actionType);
        }

        // Query with filters
        Page<ModerationAction> actions = moderationActionRepository.findWithFilters(
                userId, actionType, startDate, endDate, pageable);

        // Map to response
        return actions.map(moderationMapper::toModerationActionResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ModerationActionResponse> getUserModerationHistory(UUID userId) {
        log.info("Fetching moderation history for user {}", userId);

        // Verify user exists
        if (!userRepository.existsById(userId)) {
            throw new AppException(ErrorCode.USER_NOT_FOUND, "User not found: " + userId);
        }

        // Fetch actions chronologically (oldest to newest)
        List<ModerationAction> actions = moderationActionRepository.findByUserIdOrderByCreatedAtAsc(userId);

        // Map to response
        return actions.stream()
                .map(moderationMapper::toModerationActionResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "reportStatistics", key = "#startDate + '_' + #endDate", unless = "#result == null")
    public ReportStatisticsResponse getReportStatistics(LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Fetching report statistics from {} to {}", startDate, endDate);

        // Set default date range if not provided (last 30 days)
        if (startDate == null) {
            startDate = LocalDateTime.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDateTime.now();
        }

        // 1. Count reports by status
        long totalReports = reportContentRepository.countCreatedAfter(startDate);
        long pendingReports = reportContentRepository.countByStatus("PENDING");
        long processedReports = reportContentRepository.countByStatus("PROCESSED");
        long dismissedReports = reportContentRepository.countByStatus("DISMISSED");

        // 2. Count by report type
        Map<String, Long> byType = new HashMap<>();
        for (ReportType type : ReportType.values()) {
            long count = handleReportContentRepository.countByReportType(type.name());
            if (count > 0) {
                byType.put(type.name(), count);
            }
        }

        // 3. Count by content type
        Map<String, Long> byContentType = Map.of(
                "POST", reportContentRepository.countByContentType("POST"),
                "COMMENT", reportContentRepository.countByContentType("COMMENT"));

        // 4. Calculate average handling time
        Double avgHandlingTime = calculateAvgHandlingTime(startDate, endDate);

        // 5. Get daily trend (last 7 days)
        List<DailyTrendDto> trend = calculateDailyTrend(startDate, endDate);

        return ReportStatisticsResponse.builder()
                .totalReports(totalReports)
                .pendingReports(pendingReports)
                .processedReports(processedReports)
                .dismissedReports(dismissedReports)
                .byType(byType)
                .byContentType(byContentType)
                .avgHandlingTimeHours(avgHandlingTime)
                .trend(trend)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "userStatistics", unless = "#result == null")
    public UserStatisticsResponse getUserStatistics() {
        log.info("Fetching user statistics");

        // 1. Count users by status
        long totalUsers = userRepository.count();
        long activeUsers = userRepository.countByIsLockedAndIsDeleted(false, false);
        long lockedUsers = userRepository.countByIsLocked(true);
        long deletedUsers = userRepository.countByIsDeleted(true);

        // 2. Count new users this month
        LocalDateTime startOfMonth =
                LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        // TODO: Add countCreatedAfter() method to UserRepository
        long newUsersThisMonth = 0; // Placeholder

        // 3. Calculate growth rate
        // TODO: Compare this month vs last month
        Double growthRate = 6.7; // Placeholder

        // 4. Count by role
        // TODO: Add countByRole() method to UserRepository or query directly
        Map<String, Long> byRole = new HashMap<>();
        byRole.put("USER", activeUsers); // Simplified
        byRole.put("ADMIN", 20L); // Placeholder

        // 5. Get top reporters
        List<TopReporterDto> topReporters = getTopReporters();

        return UserStatisticsResponse.builder()
                .totalUsers(totalUsers)
                .activeUsers(activeUsers)
                .lockedUsers(lockedUsers)
                .deletedUsers(deletedUsers)
                .newUsersThisMonth(newUsersThisMonth)
                .growthRatePercent(growthRate)
                .byRole(byRole)
                .topReporters(topReporters)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(
            value = "contentStatistics",
            key = "#startDate + '_' + #endDate",
            unless = "#result == null")
    public ContentStatisticsResponse getContentStatistics(LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Fetching content statistics from {} to {}", startDate, endDate);

        // 1. Count total posts and comments
        long totalPosts = postRepository.count();
        long totalComments = commentRepository.count();

        // 2. Count content created today
        LocalDateTime startOfDay =
                LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        long postsCreatedToday = postRepository.countCreatedAfter(startOfDay);
        long commentsCreatedToday = commentRepository.countCreatedAfter(startOfDay);

        // 3. Count deleted content
        long deletedPosts = postRepository.countByIsDeleted(true);
        long deletedComments = commentRepository.countByIsDeleted(true);

        // 4. Calculate average posts per user
        long totalUsers = userRepository.count();
        double avgPostsPerUser = totalUsers > 0 ? (double) totalPosts / totalUsers : 0.0;

        // 5. Get most active users
        // TODO: Add query to PostRepository to find users with most posts
        List<MostActiveUserDto> mostActiveUsers = List.of(); // Placeholder

        return ContentStatisticsResponse.builder()
                .totalPosts(totalPosts)
                .totalComments(totalComments)
                .postsCreatedToday(postsCreatedToday)
                .commentsCreatedToday(commentsCreatedToday)
                .deletedPosts(deletedPosts)
                .deletedComments(deletedComments)
                .avgPostsPerUser(avgPostsPerUser)
                .mostActiveUsers(mostActiveUsers)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public SystemHealthResponse getSystemHealth() {
        log.info("Fetching system health metrics");

        // NOTE: This is a simplified implementation
        // Full implementation requires Spring Boot Actuator integration

        return SystemHealthResponse.builder()
                .apiUptimePercent(99.8) // TODO: Integrate with monitoring
                .avgResponseTimeMs(120) // TODO: Integrate with metrics
                .errorRatePercent(0.5) // TODO: Integrate with logging
                .dbConnectionPool(
                        Map.of(
                                "active", 5,
                                "idle", 15,
                                "max", 20)) // TODO: Get from HikariCP
                .recentErrors(List.of()) // TODO: Query error logs
                .cacheHitRatePercent(85.2) // TODO: Get from cache stats
                .build();
    }

    /**
     * Calculate average handling time for reports
     */
    private Double calculateAvgHandlingTime(LocalDateTime startDate, LocalDateTime endDate) {
        // TODO: Implement calculation based on created_date vs updated_date
        // This requires querying reports with PROCESSED/DISMISSED status
        // and calculating time difference
        return 4.5; // Placeholder
    }

    /**
     * Calculate daily trend for reports
     */
    private List<DailyTrendDto> calculateDailyTrend(LocalDateTime startDate, LocalDateTime endDate) {
        // TODO: Implement daily aggregation query
        // GROUP BY DATE(created_date) ORDER BY date DESC LIMIT 7
        return List.of(); // Placeholder
    }

    /**
     * Get top 5 users who submitted most reports
     */
    private List<TopReporterDto> getTopReporters() {
        Pageable topFive = PageRequest.of(0, 5);
        List<Object[]> results = handleReportContentRepository.findTopReporters(topFive);

        return results.stream()
                .map(row -> TopReporterDto.builder()
                        .userId(((User) row[0]).getId())
                        .username(((User) row[0]).getUsername())
                        .reportsSubmitted((Long) row[1])
                        .build())
                .collect(Collectors.toList());
    }
}
