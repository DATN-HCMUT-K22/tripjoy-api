package com.tripjoy.api.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.tripjoy.api.constant.Endpoint;
import com.tripjoy.api.dto.request.report.ModerationActionRequest;
import com.tripjoy.api.dto.response.ApiResponse;
import com.tripjoy.api.dto.response.analytics.ContentStatisticsResponse;
import com.tripjoy.api.dto.response.analytics.ReportStatisticsResponse;
import com.tripjoy.api.dto.response.analytics.SystemHealthResponse;
import com.tripjoy.api.dto.response.analytics.UserStatisticsResponse;
import com.tripjoy.api.dto.response.report.ModerationActionResponse;
import com.tripjoy.api.service.IAdminService;
import com.tripjoy.api.utils.SecurityUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping(Endpoint.Admin.BASE)
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Admin", description = "Endpoints for high-level administrative actions")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    IAdminService adminService;

    @Operation(summary = "Perform a moderation action on a user (e.g., BAN, WARN)")
    @PostMapping("/moderate-user")
    public ApiResponse<ModerationActionResponse> moderateUser(@Valid @RequestBody ModerationActionRequest request) {
        UUID adminId = SecurityUtils.getCurrentUserId();
        return ApiResponse.<ModerationActionResponse>builder()
                .data(adminService.moderateUser(request, adminId))
                .build();
    }

    @Operation(summary = "Get all moderation actions with filters (Admin)")
    @GetMapping("/moderation-actions")
    public ApiResponse<Page<ModerationActionResponse>> getModerationActions(
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) String actionType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            Pageable pageable) {
        return ApiResponse.<Page<ModerationActionResponse>>builder()
                .data(adminService.getModerationActions(userId, actionType, startDate, endDate, pageable))
                .build();
    }

    @Operation(summary = "Get moderation history for a specific user (Admin)")
    @GetMapping("/moderation-actions/user/{userId}")
    public ApiResponse<List<ModerationActionResponse>> getUserModerationHistory(@PathVariable UUID userId) {
        return ApiResponse.<List<ModerationActionResponse>>builder()
                .data(adminService.getUserModerationHistory(userId))
                .build();
    }

    @Operation(summary = "Get report statistics for admin dashboard")
    @GetMapping("/stats/reports")
    public ApiResponse<ReportStatisticsResponse> getReportStatistics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    LocalDateTime endDate) {
        return ApiResponse.<ReportStatisticsResponse>builder()
                .data(adminService.getReportStatistics(startDate, endDate))
                .build();
    }

    @Operation(summary = "Get user statistics for admin dashboard")
    @GetMapping("/stats/users")
    public ApiResponse<UserStatisticsResponse> getUserStatistics() {
        return ApiResponse.<UserStatisticsResponse>builder()
                .data(adminService.getUserStatistics())
                .build();
    }

    @Operation(summary = "Get content statistics for admin dashboard")
    @GetMapping("/stats/content")
    public ApiResponse<ContentStatisticsResponse> getContentStatistics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    LocalDateTime endDate) {
        return ApiResponse.<ContentStatisticsResponse>builder()
                .data(adminService.getContentStatistics(startDate, endDate))
                .build();
    }

    @Operation(summary = "Get system health metrics for admin dashboard")
    @GetMapping("/stats/system-health")
    public ApiResponse<SystemHealthResponse> getSystemHealth() {
        return ApiResponse.<SystemHealthResponse>builder()
                .data(adminService.getSystemHealth())
                .build();
    }
}
