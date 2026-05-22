package com.tripjoy.api.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.tripjoy.api.dto.request.report.HandleReportRequest;
import com.tripjoy.api.dto.request.report.ReportRequest;
import com.tripjoy.api.dto.response.report.HandleReportResponse;
import com.tripjoy.api.dto.response.report.ReportResponse;

public interface IReportService {

    /**
     * Submit a new content report
     */
    ReportResponse submitReport(ReportRequest request, UUID reporterId);

    /**
     * Get all reports with optional filters (admin only)
     */
    Page<ReportResponse> getAllReports(
            String status,
            String contentType,
            String reportType,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable);

    /**
     * Get report details by ID (admin only)
     */
    ReportResponse getReportById(UUID reportId);

    /**
     * Handle a report (approve/dismiss) (admin only)
     */
    HandleReportResponse handleReport(UUID reportId, HandleReportRequest request, UUID adminId);
}
