package com.tripjoy.api.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tripjoy.api.constant.Endpoint;
import com.tripjoy.api.dto.response.ApiResponse;
import com.tripjoy.api.dto.response.dashboard.AdminDashboardOverviewResponse;
import com.tripjoy.api.service.IAdminDashboardService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping(Endpoint.AdminDashboard.BASE)
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Admin Dashboard", description = "Aggregated business metrics for the admin dashboard")
@PreAuthorize("hasAuthority('READ_ADMIN_DASHBOARD')")
public class AdminDashboardController {

    IAdminDashboardService adminDashboardService;

    @GetMapping(Endpoint.AdminDashboard.OVERVIEW)
    @Operation(summary = "Get admin dashboard overview metrics")
    public ApiResponse<AdminDashboardOverviewResponse> getOverview() {
        return ApiResponse.<AdminDashboardOverviewResponse>builder()
                .data(adminDashboardService.getOverview())
                .build();
    }
}
