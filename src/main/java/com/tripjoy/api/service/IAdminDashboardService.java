package com.tripjoy.api.service;

import com.tripjoy.api.dto.response.dashboard.AdminDashboardOverviewResponse;

public interface IAdminDashboardService {

    AdminDashboardOverviewResponse getOverview();
}
