package com.tripjoy.api.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.tripjoy.api.dto.request.report.ModerationActionRequest;
import com.tripjoy.api.dto.response.analytics.*;
import com.tripjoy.api.dto.response.report.ModerationActionResponse;
import com.tripjoy.api.entity.ModerationAction;
import com.tripjoy.api.entity.User;
import com.tripjoy.api.entity.embeddable.SoftDeleteInfo;
import com.tripjoy.api.exception.AppException;
import com.tripjoy.api.mapper.ModerationMapper;
import com.tripjoy.api.repository.*;
import com.tripjoy.api.service.impl.AdminService;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private ModerationActionRepository moderationActionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ReportContentRepository reportContentRepository;

    @Mock
    private HandleReportContentRepository handleReportContentRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private ModerationMapper moderationMapper;

    @InjectMocks
    private AdminService adminService;

    private UUID adminId;
    private UUID targetUserId;
    private User admin;
    private User targetUser;
    private ModerationActionRequest request;

    @BeforeEach
    void setUp() {
        adminId = UUID.randomUUID();
        targetUserId = UUID.randomUUID();

        admin = User.builder()
                .id(adminId)
                .username("admin_user")
                .isLocked(false)
                .softDeleteInfo(new SoftDeleteInfo())
                .build();

        targetUser = User.builder()
                .id(targetUserId)
                .username("target_user")
                .isLocked(false)
                .softDeleteInfo(new SoftDeleteInfo())
                .build();

        request = ModerationActionRequest.builder()
                .userId(targetUserId.toString())
                .actionType("BAN_USER")
                .note("Violation of community guidelines")
                .build();
    }

    @Test
    void moderateUser_Success_BanUser() {
        // Given
        when(userRepository.findById(targetUserId)).thenReturn(Optional.of(targetUser));
        when(userRepository.findById(adminId)).thenReturn(Optional.of(admin));
        when(userRepository.save(any(User.class))).thenReturn(targetUser);

        ModerationAction savedAction = ModerationAction.builder()
                .id(UUID.randomUUID())
                .actionType("BAN_USER")
                .note(request.getNote())
                .user(targetUser)
                .ba(admin)
                .build();

        when(moderationActionRepository.save(any(ModerationAction.class))).thenReturn(savedAction);
        when(moderationMapper.toModerationActionResponse(any(ModerationAction.class)))
                .thenReturn(new ModerationActionResponse());

        // When
        ModerationActionResponse response = adminService.moderateUser(request, adminId);

        // Then
        assertNotNull(response);
        assertTrue(targetUser.getIsLocked()); // User should be locked
        verify(userRepository, times(1)).save(targetUser);
        verify(moderationActionRepository, times(1)).save(any(ModerationAction.class));
    }

    @Test
    void moderateUser_Success_WarnUser() {
        // Given
        request.setActionType("WARN_USER");
        when(userRepository.findById(targetUserId)).thenReturn(Optional.of(targetUser));
        when(userRepository.findById(adminId)).thenReturn(Optional.of(admin));
        when(userRepository.save(any(User.class))).thenReturn(targetUser);

        ModerationAction savedAction = ModerationAction.builder()
                .id(UUID.randomUUID())
                .actionType("WARN_USER")
                .note(request.getNote())
                .user(targetUser)
                .ba(admin)
                .build();

        when(moderationActionRepository.save(any(ModerationAction.class))).thenReturn(savedAction);
        when(moderationMapper.toModerationActionResponse(any(ModerationAction.class)))
                .thenReturn(new ModerationActionResponse());

        // When
        ModerationActionResponse response = adminService.moderateUser(request, adminId);

        // Then
        assertNotNull(response);
        assertFalse(targetUser.getIsLocked()); // User should NOT be locked for warning
        verify(moderationActionRepository, times(1)).save(any(ModerationAction.class));
    }

    @Test
    void moderateUser_InvalidActionType_ThrowsAppException() {
        // Given
        request.setActionType("INVALID_ACTION");

        // When & Then
        assertThrows(AppException.class, () -> {
            adminService.moderateUser(request, adminId);
        });
    }

    @Test
    void moderateUser_UserNotFound_ThrowsAppException() {
        // Given
        when(userRepository.findById(targetUserId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(AppException.class, () -> {
            adminService.moderateUser(request, adminId);
        });
    }

    @Test
    void getModerationActions_Success() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);
        ModerationAction action = ModerationAction.builder()
                .id(UUID.randomUUID())
                .actionType("BAN_USER")
                .user(targetUser)
                .ba(admin)
                .build();

        Page<ModerationAction> page = new PageImpl<>(List.of(action));

        when(moderationActionRepository.findWithFilters(
                        eq(targetUserId), eq("BAN_USER"), any(), any(), eq(pageable)))
                .thenReturn(page);
        when(moderationMapper.toModerationActionResponse(any(ModerationAction.class)))
                .thenReturn(new ModerationActionResponse());

        // When
        Page<ModerationActionResponse> response = adminService.getModerationActions(
                targetUserId, "BAN_USER", null, null, pageable);

        // Then
        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
        verify(moderationActionRepository, times(1))
                .findWithFilters(eq(targetUserId), eq("BAN_USER"), any(), any(), eq(pageable));
    }

    @Test
    void getUserModerationHistory_Success() {
        // Given
        ModerationAction action = ModerationAction.builder()
                .id(UUID.randomUUID())
                .actionType("BAN_USER")
                .user(targetUser)
                .ba(admin)
                .build();

        when(userRepository.existsById(targetUserId)).thenReturn(true);
        when(moderationActionRepository.findByUserIdOrderByCreatedAtAsc(targetUserId))
                .thenReturn(List.of(action));
        when(moderationMapper.toModerationActionResponse(any(ModerationAction.class)))
                .thenReturn(new ModerationActionResponse());

        // When
        List<ModerationActionResponse> response = adminService.getUserModerationHistory(targetUserId);

        // Then
        assertNotNull(response);
        assertEquals(1, response.size());
        verify(moderationActionRepository, times(1)).findByUserIdOrderByCreatedAtAsc(targetUserId);
    }

    @Test
    void getUserModerationHistory_UserNotFound_ThrowsAppException() {
        // Given
        when(userRepository.existsById(targetUserId)).thenReturn(false);

        // When & Then
        assertThrows(AppException.class, () -> {
            adminService.getUserModerationHistory(targetUserId);
        });
    }

    @Test
    void getReportStatistics_Success() {
        // Given
        LocalDateTime startDate = LocalDateTime.now().minusDays(30);
        LocalDateTime endDate = LocalDateTime.now();

        when(reportContentRepository.countCreatedAfter(any())).thenReturn(100L);
        when(reportContentRepository.countByStatus("PENDING")).thenReturn(20L);
        when(reportContentRepository.countByStatus("PROCESSED")).thenReturn(60L);
        when(reportContentRepository.countByStatus("DISMISSED")).thenReturn(20L);
        when(reportContentRepository.countByContentType("POST")).thenReturn(70L);
        when(reportContentRepository.countByContentType("COMMENT")).thenReturn(30L);
        when(handleReportContentRepository.countByReportType(anyString())).thenReturn(10L);

        // When
        ReportStatisticsResponse response = adminService.getReportStatistics(startDate, endDate);

        // Then
        assertNotNull(response);
        assertEquals(100L, response.getTotalReports());
        assertEquals(20L, response.getPendingReports());
        assertEquals(60L, response.getProcessedReports());
        assertEquals(20L, response.getDismissedReports());
        assertNotNull(response.getByType());
        assertNotNull(response.getByContentType());
    }

    @Test
    void getUserStatistics_Success() {
        // Given
        when(userRepository.count()).thenReturn(5000L);
        when(userRepository.countByIsLockedAndIsDeleted(false, false)).thenReturn(4800L);
        when(userRepository.countByIsLocked(true)).thenReturn(150L);
        when(userRepository.countByIsDeleted(true)).thenReturn(50L);
        when(handleReportContentRepository.findTopReporters(any())).thenReturn(List.of());

        // When
        UserStatisticsResponse response = adminService.getUserStatistics();

        // Then
        assertNotNull(response);
        assertEquals(5000L, response.getTotalUsers());
        assertEquals(4800L, response.getActiveUsers());
        assertEquals(150L, response.getLockedUsers());
        assertEquals(50L, response.getDeletedUsers());
        assertNotNull(response.getByRole());
    }

    @Test
    void getContentStatistics_Success() {
        // Given
        when(postRepository.count()).thenReturn(10000L);
        when(commentRepository.count()).thenReturn(25000L);
        when(postRepository.countCreatedAfter(any())).thenReturn(50L);
        when(commentRepository.countCreatedAfter(any())).thenReturn(120L);
        when(postRepository.countByIsDeleted(true)).thenReturn(100L);
        when(commentRepository.countByIsDeleted(true)).thenReturn(200L);
        when(userRepository.count()).thenReturn(5000L);

        // When
        ContentStatisticsResponse response = adminService.getContentStatistics(null, null);

        // Then
        assertNotNull(response);
        assertEquals(10000L, response.getTotalPosts());
        assertEquals(25000L, response.getTotalComments());
        assertEquals(50L, response.getPostsCreatedToday());
        assertEquals(120L, response.getCommentsCreatedToday());
        assertEquals(2.0, response.getAvgPostsPerUser());
    }

    @Test
    void getSystemHealth_Success() {
        // When
        SystemHealthResponse response = adminService.getSystemHealth();

        // Then
        assertNotNull(response);
        assertNotNull(response.getApiUptimePercent());
        assertNotNull(response.getAvgResponseTimeMs());
        assertNotNull(response.getDbConnectionPool());
    }
}
