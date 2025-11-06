package com.tripjoy.api.controller;

import com.tripjoy.api.constant.Endpoint;
import com.tripjoy.api.dto.request.report.ModerationActionRequest;
import com.tripjoy.api.dto.response.ApiResponse;
import com.tripjoy.api.dto.response.report.ModerationActionResponse;
import com.tripjoy.api.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(Endpoint.Admin.BASE)
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Admin", description = "Endpoints for high-level administrative actions")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    AdminService adminService;

    @Operation(summary = "Perform a moderation action on a user (e.g., BAN, WARN)")
    @PostMapping("/moderate-user")
    public ApiResponse<ModerationActionResponse> moderateUser(
            @Valid @RequestBody ModerationActionRequest request) {

        // return ApiResponse.<ModerationActionResponse>builder()
        //        .data(adminService.moderateUser(request))
        //        .build();
        return null; // Placeholder
    }
}