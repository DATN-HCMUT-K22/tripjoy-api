package com.tripjoy.api.controller;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import com.tripjoy.api.configuration.security.CustomJwtDecoder;
import com.tripjoy.api.configuration.security.SecurityConfig;
import com.tripjoy.api.dto.response.dashboard.AdminDashboardOverviewResponse;
import com.tripjoy.api.service.IAdminDashboardService;

@WebMvcTest(AdminDashboardController.class)
@Import(SecurityConfig.class)
class AdminDashboardControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    IAdminDashboardService adminDashboardService;

    @MockBean
    CustomJwtDecoder customJwtDecoder;

    @Test
    void getOverview_withDashboardPermission_returnsMetrics() throws Exception {
        when(adminDashboardService.getOverview()).thenReturn(createOverviewResponse());

        mockMvc.perform(get("/api/v1/admin/dashboard/overview")
                        .with(user("business-admin").authorities(() -> "READ_ADMIN_DASHBOARD")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.data.users.total").value(100))
                .andExpect(jsonPath("$.data.users.locked").value(4))
                .andExpect(jsonPath("$.data.content.posts").value(80))
                .andExpect(jsonPath("$.data.moderation.pendingReports").value(5))
                .andExpect(jsonPath("$.data.moderation.totalActions").value(11))
                .andExpect(jsonPath("$.data.generatedAt").value("2026-06-01T03:30:00Z"));
    }

    @Test
    void getOverview_withoutDashboardPermission_returnsForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/admin/dashboard/overview")
                        .with(user("standard-user").roles("USER")))
                .andExpect(status().isForbidden());
    }

    private AdminDashboardOverviewResponse createOverviewResponse() {
        return AdminDashboardOverviewResponse.builder()
                .users(AdminDashboardOverviewResponse.UserMetrics.builder()
                        .total(100)
                        .locked(4)
                        .build())
                .content(AdminDashboardOverviewResponse.ContentMetrics.builder()
                        .posts(80)
                        .comments(120)
                        .itineraries(32)
                        .groups(12)
                        .build())
                .moderation(AdminDashboardOverviewResponse.ModerationMetrics.builder()
                        .pendingReports(5)
                        .processedReports(20)
                        .dismissedReports(3)
                        .totalActions(11)
                        .build())
                .generatedAt(Instant.parse("2026-06-01T03:30:00Z"))
                .build();
    }
}
