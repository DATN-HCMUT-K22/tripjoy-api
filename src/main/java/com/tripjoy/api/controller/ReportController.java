package com.tripjoy.api.controller;

import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.tripjoy.api.constant.Endpoint;
import com.tripjoy.api.dto.request.report.HandleReportRequest;
import com.tripjoy.api.dto.request.report.ReportRequest;
import com.tripjoy.api.dto.response.ApiResponse;
import com.tripjoy.api.dto.response.report.HandleReportResponse;
import com.tripjoy.api.dto.response.report.ReportResponse;
import com.tripjoy.api.service.IReportService;

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
        return ApiResponse.<ReportResponse>builder()
                .data(reportService.submitReport(request))
                .build();
    }

    @Operation(summary = "Get all reports (Admin/Business Admin, paginated)")
    @GetMapping
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','BUSINESS_ADMIN')")
    public ApiResponse<Page<ReportResponse>> getAllReports(Pageable pageable) {
        return ApiResponse.<Page<ReportResponse>>builder()
                .data(reportService.getAllReports(pageable))
                .build();
    }

    @Operation(summary = "Get report details by id (Admin/Business Admin)")
    @GetMapping(Endpoint.Report.ID)
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','BUSINESS_ADMIN')")
    public ApiResponse<ReportResponse> getReportById(@PathVariable("reportId") UUID reportId) {
        return ApiResponse.<ReportResponse>builder()
                .data(reportService.getReportById(reportId))
                .build();
    }

    @Operation(summary = "Admin/Business Admin handles a report")
    @PostMapping(Endpoint.Report.ID + "/handle")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','BUSINESS_ADMIN')")
    public ApiResponse<HandleReportResponse> handleReport(
            @PathVariable("reportId") UUID reportId, @Valid @RequestBody HandleReportRequest request) {
        return ApiResponse.<HandleReportResponse>builder()
                .data(reportService.handleReport(reportId, request))
                .build();
    }
}
