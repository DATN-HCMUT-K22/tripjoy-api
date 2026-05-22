package com.tripjoy.api.service.impl;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tripjoy.api.constant.ReportStatus;
import com.tripjoy.api.constant.ReportType;
import com.tripjoy.api.dto.request.report.HandleReportRequest;
import com.tripjoy.api.dto.request.report.ReportRequest;
import com.tripjoy.api.dto.response.report.HandleReportResponse;
import com.tripjoy.api.dto.response.report.ReportResponse;
import com.tripjoy.api.entity.*;
import com.tripjoy.api.exception.AppException;
import com.tripjoy.api.exception.ErrorCode;
import com.tripjoy.api.mapper.ReportMapper;
import com.tripjoy.api.repository.*;
import com.tripjoy.api.service.IReportService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ReportService implements IReportService {

    HandleReportContentRepository handleReportContentRepository;
    ReportContentRepository reportContentRepository;
    PostRepository postRepository;
    CommentRepository commentRepository;
    UserRepository userRepository;
    ReportMapper reportMapper;

    @Override
    @Transactional
    public ReportResponse submitReport(ReportRequest request, UUID reporterId) {
        log.info("User {} submitting report for {} {}",
                 reporterId, request.getContentType(), request.getContentId());

        // 1. Validate report type
        if (!ReportType.isValid(request.getReportType())) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Invalid report type: " + request.getReportType());
        }

        // 2. Fetch reporter
        User reporter = userRepository.findById(reporterId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // 3. Fetch content and create snapshot
        ReportContent snapshot = createContentSnapshot(request.getContentId(), request.getContentType());

        // 4. Save snapshot
        ReportContent savedSnapshot = reportContentRepository.save(snapshot);

        // 5. Create report submission
        HandleReportContent report = HandleReportContent.builder()
                .report_type(request.getReportType())
                .description(request.getDescription())
                .reportContent(savedSnapshot)
                .ba(reporter)
                .build();

        // 6. Save report
        HandleReportContent savedReport = handleReportContentRepository.save(report);

        log.info("Report {} created successfully for {} {}",
                 savedReport.getId(), request.getContentType(), request.getContentId());

        // 7. Map to response
        return reportMapper.toReportResponse(savedReport);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReportResponse> getAllReports(
            String status,
            String contentType,
            String reportType,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable) {

        log.info("Fetching reports with filters - status: {}, contentType: {}, reportType: {}",
                 status, contentType, reportType);

        // Validate filters
        if (status != null && !ReportStatus.isValid(status)) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Invalid status: " + status);
        }

        if (reportType != null && !ReportType.isValid(reportType)) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Invalid report type: " + reportType);
        }

        // Fetch with filters
        Page<HandleReportContent> reports = handleReportContentRepository.findWithFilters(
                status, reportType, startDate, endDate, pageable);

        // Map to response
        return reports.map(reportMapper::toReportResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ReportResponse getReportById(UUID reportId) {
        log.info("Fetching report details for ID: {}", reportId);

        HandleReportContent report = handleReportContentRepository.findByIdWithDetails(reportId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND,
                                                    "Report not found with ID: " + reportId));

        return reportMapper.toReportResponse(report);
    }

    @Override
    @Transactional
    public HandleReportResponse handleReport(UUID reportId, HandleReportRequest request, UUID adminId) {
        log.info("Admin {} handling report {}", adminId, reportId);

        // 1. Validate status
        if (!ReportStatus.isValid(request.getStatus())) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Invalid status: " + request.getStatus());
        }

        // 2. Fetch report with details
        HandleReportContent report = handleReportContentRepository.findByIdWithDetails(reportId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND,
                                                    "Report not found with ID: " + reportId));

        // 3. Check if already handled
        String currentStatus = report.getReportContent().getStatus();
        if (!"PENDING".equalsIgnoreCase(currentStatus)) {
            throw new AppException(ErrorCode.INVALID_REQUEST,
                                  "Report already handled with status: " + currentStatus);
        }

        // 4. Update report status
        report.getReportContent().setStatus(request.getStatus().toUpperCase());

        // 5. Update description if provided
        if (request.getDescription() != null && !request.getDescription().isBlank()) {
            report.setDescription(request.getDescription());
        }

        // 6. Save changes
        handleReportContentRepository.save(report);

        log.info("Report {} handled successfully with status {}", reportId, request.getStatus());

        // 7. Map to response
        return reportMapper.toHandleReportResponse(report);
    }

    /**
     * Create content snapshot from Post or Comment
     */
    private ReportContent createContentSnapshot(UUID contentId, String contentType) {
        if ("POST".equalsIgnoreCase(contentType)) {
            Post post = postRepository.findById(contentId)
                    .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND,
                                                       "Post not found with ID: " + contentId));

            String mediaUrl = (post.getMediaUrls() != null && !post.getMediaUrls().isEmpty())
                    ? post.getMediaUrls().get(0)
                    : null;

            return ReportContent.builder()
                    .contentType("POST")
                    .text(post.getContent())
                    .mediaUrl(mediaUrl)
                    .status(ReportStatus.PENDING.name())
                    .build();

        } else if ("COMMENT".equalsIgnoreCase(contentType)) {
            Comment comment = commentRepository.findById(contentId)
                    .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND,
                                                       "Comment not found with ID: " + contentId));

            return ReportContent.builder()
                    .contentType("COMMENT")
                    .text(comment.getContent())
                    .mediaUrl(null)
                    .status(ReportStatus.PENDING.name())
                    .build();

        } else {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Invalid content type: " + contentType);
        }
    }
}
