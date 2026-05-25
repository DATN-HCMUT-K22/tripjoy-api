package com.tripjoy.api.controller;

import jakarta.validation.Valid;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.tripjoy.api.constant.Endpoint;
import com.tripjoy.api.dto.request.report.ModerationActionRequest;
import com.tripjoy.api.dto.response.ApiResponse;
import com.tripjoy.api.dto.response.report.ModerationActionResponse;
import com.tripjoy.api.service.IAdminService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping(Endpoint.Admin.BASE)
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Admin", description = "Endpoints for high-level administrative actions")
@PreAuthorize("hasAnyRole('SYSTEM_ADMIN','BUSINESS_ADMIN')")
public class AdminController {

    IAdminService adminService;

    @Operation(summary = "Perform a moderation action on a user (e.g., BAN, WARN)")
    @PostMapping("/moderate-user")
    public ApiResponse<ModerationActionResponse> moderateUser(@Valid @RequestBody ModerationActionRequest request) {
        return ApiResponse.<ModerationActionResponse>builder()
                .data(adminService.moderateUser(request))
                .build();
    }

    @Operation(summary = "Get a paginated and filterable list of moderation actions")
    @GetMapping("/moderation-actions")
    public ApiResponse<Page<ModerationActionResponse>> getModerationActions(
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) String actionType,
            @RequestParam(required = false) UUID baId,
            Pageable pageable) {
        return ApiResponse.<Page<ModerationActionResponse>>builder()
                .data(adminService.getModerationActions(userId, actionType, baId, pageable))
                .build();
    }
}

