package com.tripjoy.api.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.tripjoy.api.dto.request.report.HandleReportRequest;
import com.tripjoy.api.dto.request.report.ReportRequest;
import com.tripjoy.api.dto.response.report.HandleReportResponse;
import com.tripjoy.api.dto.response.report.ReportResponse;

public interface IReportService {
    ReportResponse submitReport(ReportRequest request);

    Page<ReportResponse> getAllReports(Pageable pageable);

    ReportResponse getReportById(UUID reportId);

    HandleReportResponse handleReport(UUID reportId, HandleReportRequest request);
}
