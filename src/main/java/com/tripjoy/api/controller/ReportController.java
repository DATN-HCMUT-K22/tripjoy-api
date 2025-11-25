package com.tripjoy.api.controller;

import com.tripjoy.api.constant.Endpoint;
import com.tripjoy.api.dto.request.report.HandleReportRequest;
import com.tripjoy.api.dto.request.report.ReportRequest;
import com.tripjoy.api.dto.response.ApiResponse;
import com.tripjoy.api.dto.response.report.HandleReportResponse;
import com.tripjoy.api.dto.response.report.ReportResponse;
import com.tripjoy.api.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(Endpoint.Report.BASE)
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Report", description = "Endpoints for reporting inappropriate content")
public class ReportController {

    ReportService reportService;

    @Operation(summary = "Users submits a new report")
    @PostMapping
    public ApiResponse<ReportResponse> submitReport(@Valid @RequestBody ReportRequest request) {
        // Service will handle complex logic (create Report_content, then create Report_to)
        return ApiResponse.<ReportResponse>builder()
//                .data(reportService.submitReport(request))
                .build();
    }

    @Operation(summary = "Get all reports (Admin, paginated)")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Page<ReportResponse>> getAllReports(Pageable pageable) {
        return ApiResponse.<Page<ReportResponse>>builder()
//                .data(reportService.getAllReports(pageable))
                .build();
    }

    @Operation(summary = "Get report details by id (Admin)")
    @GetMapping("/{reportId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<ReportResponse> getReportById(@PathVariable String reportId) {
        // reportId here refers to the ID in the "Report_to" table
        return ApiResponse.<ReportResponse>builder()
//                .data(reportService.getReportById(reportId))
                .build();
    }

    // --- BỔ SUNG: Admin Handling ---

    @Operation(summary = "Admin handles a report (e.g., approve, reject)")
    @PostMapping(Endpoint.Report.ID + "/handle")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<HandleReportResponse> handleReport(
            @PathVariable String reportId, // ID from Report_to
            @Valid @RequestBody HandleReportRequest request) {

        // return ApiResponse.<HandleReportResponse>builder()
        //        .data(reportService.handleReport(reportId, request))
        //        .build();
        return null; // Placeholder
    }
}