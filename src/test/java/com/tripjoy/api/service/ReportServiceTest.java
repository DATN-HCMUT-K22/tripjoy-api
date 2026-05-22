package com.tripjoy.api.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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

import com.tripjoy.api.dto.request.report.HandleReportRequest;
import com.tripjoy.api.dto.request.report.ReportRequest;
import com.tripjoy.api.dto.response.report.HandleReportResponse;
import com.tripjoy.api.dto.response.report.ReportResponse;
import com.tripjoy.api.entity.*;
import com.tripjoy.api.entity.embeddable.SoftDeleteInfo;
import com.tripjoy.api.exception.AppException;
import com.tripjoy.api.mapper.ReportMapper;
import com.tripjoy.api.repository.*;
import com.tripjoy.api.service.impl.ReportService;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private HandleReportContentRepository handleReportContentRepository;

    @Mock
    private ReportContentRepository reportContentRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ReportMapper reportMapper;

    @InjectMocks
    private ReportService reportService;

    private UUID reporterId;
    private UUID postId;
    private User reporter;
    private Post post;
    private ReportRequest reportRequest;
    private ReportContent reportContent;
    private HandleReportContent handleReportContent;

    @BeforeEach
    void setUp() {
        reporterId = UUID.randomUUID();
        postId = UUID.randomUUID();

        reporter = User.builder()
                .id(reporterId)
                .username("reporter_user")
                .softDeleteInfo(new SoftDeleteInfo())
                .build();

        post = Post.builder()
                .id(postId)
                .content("Test post content")
                .softDeleteInfo(new SoftDeleteInfo())
                .build();

        reportRequest = ReportRequest.builder()
                .contentId(postId)
                .contentType("POST")
                .reportType("SPAM")
                .description("This is spam")
                .build();

        reportContent = ReportContent.builder()
                .id(UUID.randomUUID())
                .contentType("POST")
                .text("Test post content")
                .status("PENDING")
                .build();

        handleReportContent = HandleReportContent.builder()
                .id(UUID.randomUUID())
                .report_type("SPAM")
                .description("This is spam")
                .reportContent(reportContent)
                .ba(reporter)
                .build();
    }

    @Test
    void submitReport_Success_PostReport() {
        // Given
        when(userRepository.findById(reporterId)).thenReturn(Optional.of(reporter));
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(reportContentRepository.save(any(ReportContent.class))).thenReturn(reportContent);
        when(handleReportContentRepository.save(any(HandleReportContent.class)))
                .thenReturn(handleReportContent);
        when(reportMapper.toReportResponse(any(HandleReportContent.class)))
                .thenReturn(new ReportResponse());

        // When
        ReportResponse response = reportService.submitReport(reportRequest, reporterId);

        // Then
        assertNotNull(response);
        verify(userRepository, times(1)).findById(reporterId);
        verify(postRepository, times(1)).findById(postId);
        verify(reportContentRepository, times(1)).save(any(ReportContent.class));
        verify(handleReportContentRepository, times(1)).save(any(HandleReportContent.class));
    }

    @Test
    void submitReport_Success_CommentReport() {
        // Given
        UUID commentId = UUID.randomUUID();
        Comment comment = Comment.builder()
                .id(commentId)
                .content("Test comment")
                .build();

        reportRequest.setContentId(commentId);
        reportRequest.setContentType("COMMENT");

        when(userRepository.findById(reporterId)).thenReturn(Optional.of(reporter));
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
        when(reportContentRepository.save(any(ReportContent.class))).thenReturn(reportContent);
        when(handleReportContentRepository.save(any(HandleReportContent.class)))
                .thenReturn(handleReportContent);
        when(reportMapper.toReportResponse(any(HandleReportContent.class)))
                .thenReturn(new ReportResponse());

        // When
        ReportResponse response = reportService.submitReport(reportRequest, reporterId);

        // Then
        assertNotNull(response);
        verify(commentRepository, times(1)).findById(commentId);
    }

    @Test
    void submitReport_InvalidReportType_ThrowsAppException() {
        // Given
        reportRequest.setReportType("INVALID_TYPE");

        // When & Then
        assertThrows(AppException.class, () -> {
            reportService.submitReport(reportRequest, reporterId);
        });
    }

    @Test
    void submitReport_InvalidContentType_ThrowsAppException() {
        // Given
        reportRequest.setContentType("INVALID_CONTENT");

        // When & Then
        assertThrows(AppException.class, () -> {
            reportService.submitReport(reportRequest, reporterId);
        });
    }

    @Test
    void submitReport_ReporterNotFound_ThrowsAppException() {
        // Given
        when(userRepository.findById(reporterId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(AppException.class, () -> {
            reportService.submitReport(reportRequest, reporterId);
        });
    }

    @Test
    void submitReport_PostNotFound_ThrowsAppException() {
        // Given
        when(userRepository.findById(reporterId)).thenReturn(Optional.of(reporter));
        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(AppException.class, () -> {
            reportService.submitReport(reportRequest, reporterId);
        });
    }

    @Test
    void getAllReports_Success_WithFilters() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);
        Page<HandleReportContent> page = new PageImpl<>(List.of(handleReportContent));

        when(handleReportContentRepository.findWithFilters(
                        eq("PENDING"), eq("SPAM"), any(), any(), eq(pageable)))
                .thenReturn(page);
        when(reportMapper.toReportResponse(any(HandleReportContent.class)))
                .thenReturn(new ReportResponse());

        // When
        Page<ReportResponse> response = reportService.getAllReports(
                "PENDING", "POST", "SPAM", null, null, pageable);

        // Then
        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
        verify(handleReportContentRepository, times(1))
                .findWithFilters(eq("PENDING"), eq("SPAM"), any(), any(), eq(pageable));
    }

    @Test
    void getReportById_Success() {
        // Given
        UUID reportId = UUID.randomUUID();
        when(handleReportContentRepository.findByIdWithDetails(reportId))
                .thenReturn(Optional.of(handleReportContent));
        when(reportMapper.toReportResponse(handleReportContent))
                .thenReturn(new ReportResponse());

        // When
        ReportResponse response = reportService.getReportById(reportId);

        // Then
        assertNotNull(response);
        verify(handleReportContentRepository, times(1)).findByIdWithDetails(reportId);
    }

    @Test
    void getReportById_NotFound_ThrowsAppException() {
        // Given
        UUID reportId = UUID.randomUUID();
        when(handleReportContentRepository.findByIdWithDetails(reportId))
                .thenReturn(Optional.empty());

        // When & Then
        assertThrows(AppException.class, () -> {
            reportService.getReportById(reportId);
        });
    }

    @Test
    void handleReport_Success_ProcessedStatus() {
        // Given
        UUID reportId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();

        HandleReportRequest request = HandleReportRequest.builder()
                .status("PROCESSED")
                .description("Report processed")
                .build();

        when(handleReportContentRepository.findByIdWithDetails(reportId))
                .thenReturn(Optional.of(handleReportContent));
        when(handleReportContentRepository.save(any(HandleReportContent.class)))
                .thenReturn(handleReportContent);
        when(reportMapper.toHandleReportResponse(any(HandleReportContent.class)))
                .thenReturn(new HandleReportResponse());

        // When
        HandleReportResponse response = reportService.handleReport(reportId, request, adminId);

        // Then
        assertNotNull(response);
        assertEquals("PROCESSED", reportContent.getStatus());
        verify(handleReportContentRepository, times(1)).save(handleReportContent);
    }

    @Test
    void handleReport_Success_DismissedStatus() {
        // Given
        UUID reportId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();

        HandleReportRequest request = HandleReportRequest.builder()
                .status("DISMISSED")
                .description("Report dismissed")
                .build();

        when(handleReportContentRepository.findByIdWithDetails(reportId))
                .thenReturn(Optional.of(handleReportContent));
        when(handleReportContentRepository.save(any(HandleReportContent.class)))
                .thenReturn(handleReportContent);
        when(reportMapper.toHandleReportResponse(any(HandleReportContent.class)))
                .thenReturn(new HandleReportResponse());

        // When
        HandleReportResponse response = reportService.handleReport(reportId, request, adminId);

        // Then
        assertNotNull(response);
        assertEquals("DISMISSED", reportContent.getStatus());
        verify(handleReportContentRepository, times(1)).save(handleReportContent);
    }

    @Test
    void handleReport_InvalidStatus_ThrowsAppException() {
        // Given
        UUID reportId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();

        HandleReportRequest request = HandleReportRequest.builder()
                .status("INVALID_STATUS")
                .build();

        // When & Then
        assertThrows(AppException.class, () -> {
            reportService.handleReport(reportId, request, adminId);
        });
    }

    @Test
    void handleReport_ReportNotFound_ThrowsAppException() {
        // Given
        UUID reportId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();

        HandleReportRequest request = HandleReportRequest.builder()
                .status("PROCESSED")
                .build();

        when(handleReportContentRepository.findByIdWithDetails(reportId))
                .thenReturn(Optional.empty());

        // When & Then
        assertThrows(AppException.class, () -> {
            reportService.handleReport(reportId, request, adminId);
        });
    }

    @Test
    void handleReport_AlreadyHandled_ThrowsAppException() {
        // Given
        UUID reportId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();

        reportContent.setStatus("PROCESSED"); // Already handled

        HandleReportRequest request = HandleReportRequest.builder()
                .status("PROCESSED")
                .build();

        when(handleReportContentRepository.findByIdWithDetails(reportId))
                .thenReturn(Optional.of(handleReportContent));

        // When & Then
        assertThrows(AppException.class, () -> {
            reportService.handleReport(reportId, request, adminId);
        });
    }
}
