package com.tripjoy.api.service.impl;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.tripjoy.api.dto.projection.AdminDashboardOverviewProjection;
import com.tripjoy.api.dto.response.dashboard.AdminDashboardOverviewResponse;
import com.tripjoy.api.repository.AdminDashboardRepository;

@ExtendWith(MockitoExtension.class)
class AdminDashboardServiceTest {

    @Mock
    AdminDashboardRepository adminDashboardRepository;

    @InjectMocks
    AdminDashboardService adminDashboardService;

    @Test
    void getOverview_mapsAggregateSnapshot() {
        AdminDashboardOverviewProjection overview = mock(AdminDashboardOverviewProjection.class);
        when(overview.getTotalUsers()).thenReturn(100L);
        when(overview.getLockedUsers()).thenReturn(4L);
        when(overview.getTotalPosts()).thenReturn(80L);
        when(overview.getTotalComments()).thenReturn(120L);
        when(overview.getTotalItineraries()).thenReturn(32L);
        when(overview.getTotalGroups()).thenReturn(12L);
        when(overview.getPendingReports()).thenReturn(5L);
        when(overview.getProcessedReports()).thenReturn(20L);
        when(overview.getDismissedReports()).thenReturn(3L);
        when(overview.getTotalModerationActions()).thenReturn(11L);
        when(adminDashboardRepository.getOverview()).thenReturn(overview);

        AdminDashboardOverviewResponse response = adminDashboardService.getOverview();

        assertAll(
                () -> assertEquals(100, response.getUsers().getTotal()),
                () -> assertEquals(4, response.getUsers().getLocked()),
                () -> assertEquals(80, response.getContent().getPosts()),
                () -> assertEquals(120, response.getContent().getComments()),
                () -> assertEquals(32, response.getContent().getItineraries()),
                () -> assertEquals(12, response.getContent().getGroups()),
                () -> assertEquals(5, response.getModeration().getPendingReports()),
                () -> assertEquals(20, response.getModeration().getProcessedReports()),
                () -> assertEquals(3, response.getModeration().getDismissedReports()),
                () -> assertEquals(11, response.getModeration().getTotalActions()),
                () -> assertNotNull(response.getGeneratedAt()));
    }
}
