package com.tripjoy.api.controller;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.tripjoy.api.constant.Endpoint;
import com.tripjoy.api.dto.request.report.HandleReportRequest;
import com.tripjoy.api.dto.request.report.ReportRequest;
import com.tripjoy.api.dto.response.ApiResponse;
import com.tripjoy.api.dto.response.report.HandleReportResponse;
import com.tripjoy.api.dto.response.report.ReportResponse;
import com.tripjoy.api.service.IReportService;
import com.tripjoy.api.utils.SecurityUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping(Endpoint.Report.BASE)
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Report", description = "Endpoints for reporting inappropriate content")
public class ReportController {

    IReportService reportService;

    @Operation(summary = "User submits a new report")
    @PostMapping
    public ApiResponse<ReportResponse> submitReport(@Valid @RequestBody ReportRequest request) {
        UUID reporterId = SecurityUtils.getCurrentUserId();
        return ApiResponse.<ReportResponse>builder()
                .data(reportService.submitReport(request, reporterId))
                .build();
    }

    @Operation(summary = "Get all reports (Admin, paginated)")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Page<ReportResponse>> getAllReports(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String contentType,
            @RequestParam(required = false) String reportType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            Pageable pageable) {
        return ApiResponse.<Page<ReportResponse>>builder()
                .data(reportService.getAllReports(status, contentType, reportType, startDate, endDate, pageable))
                .build();
    }

    @Operation(summary = "Get report details by id (Admin)")
    @GetMapping("/{reportId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<ReportResponse> getReportById(@PathVariable("reportId") UUID reportId) {
        return ApiResponse.<ReportResponse>builder()
                .data(reportService.getReportById(reportId))
                .build();
    }

    // --- BỔ SUNG: Admin Handling ---

    @Operation(summary = "Admin handles a report (e.g., approve, reject)")
    @PostMapping(Endpoint.Report.ID + "/handle")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<HandleReportResponse> handleReport(
            @PathVariable("reportId") UUID reportId,
            @Valid @RequestBody HandleReportRequest request) {
        UUID adminId = SecurityUtils.getCurrentUserId();
        return ApiResponse.<HandleReportResponse>builder()
                .data(reportService.handleReport(reportId, request, adminId))
                .build();
    }
}
