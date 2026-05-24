package com.tripjoy.api.controller;

import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.tripjoy.api.constant.Endpoint;
import com.tripjoy.api.dto.request.feedback.FeedbackRequest;
import com.tripjoy.api.dto.request.feedback.FeedbackResponseRequest;
import com.tripjoy.api.dto.response.ApiResponse;
import com.tripjoy.api.dto.response.feedback.FeedbackResponse;
import com.tripjoy.api.dto.response.feedback.FeedbackResponseResponse;
import com.tripjoy.api.service.IFeedbackService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping(Endpoint.Feedback.BASE)
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Feedback", description = "Endpoints for submitting and managing feedback")
public class FeedbackController {

    IFeedbackService feedbackService;

    @Operation(summary = "Submit new feedback")
    @PostMapping
    public ApiResponse<FeedbackResponse> submitFeedback(@Valid @RequestBody FeedbackRequest request) {
        return ApiResponse.<FeedbackResponse>builder()
                .data(feedbackService.submitFeedback(request))
                .build();
    }

    @Operation(summary = "Get all feedback (Admin/Business Admin, paginated)")
    @GetMapping
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','BUSINESS_ADMIN')")
    public ApiResponse<Page<FeedbackResponse>> getAllFeedback(Pageable pageable) {
        return ApiResponse.<Page<FeedbackResponse>>builder()
                .data(feedbackService.getAllFeedback(pageable))
                .build();
    }

    @Operation(summary = "Get feedback details by id (Admin/Business Admin)")
    @GetMapping(Endpoint.Feedback.ID)
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','BUSINESS_ADMIN')")
    public ApiResponse<FeedbackResponse> getFeedbackById(@PathVariable("feedbackId") UUID feedbackId) {
        return ApiResponse.<FeedbackResponse>builder()
                .data(feedbackService.getFeedbackById(feedbackId))
                .build();
    }

    @Operation(summary = "Admin/Business Admin responds to a user's feedback")
    @PostMapping(Endpoint.Feedback.ID + "/respond")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','BUSINESS_ADMIN')")
    public ApiResponse<FeedbackResponseResponse> respondToFeedback(
            @PathVariable("feedbackId") UUID feedbackId, @Valid @RequestBody FeedbackResponseRequest request) {
        return ApiResponse.<FeedbackResponseResponse>builder()
                .data(feedbackService.respondToFeedback(feedbackId, request))
                .build();
    }
}
