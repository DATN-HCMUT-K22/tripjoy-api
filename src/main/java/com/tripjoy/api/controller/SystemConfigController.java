package com.tripjoy.api.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.tripjoy.api.constant.Endpoint;
import com.tripjoy.api.dto.request.SystemConfigUpdateRequest;
import com.tripjoy.api.dto.response.ApiResponse;
import com.tripjoy.api.dto.response.SystemConfigResponse;
import com.tripjoy.api.service.ISystemConfigService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "System Config", description = "Endpoints for managing dynamic system parameters")
public class SystemConfigController {

    ISystemConfigService systemConfigService;

    @Operation(summary = "Get all system configs (Admin only)")
    @GetMapping(Endpoint.SystemConfig.BASE)
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<SystemConfigResponse>> getAll() {
        return ApiResponse.<List<SystemConfigResponse>>builder()
                .data(systemConfigService.getAllConfigs())
                .build();
    }

    @Operation(summary = "Update a system config (Admin only)")
    @PatchMapping(Endpoint.SystemConfig.BASE + Endpoint.SystemConfig.KEY)
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<SystemConfigResponse> update(
            @PathVariable("key") String key,
            @RequestBody SystemConfigUpdateRequest request) {
        return ApiResponse.<SystemConfigResponse>builder()
                .data(systemConfigService.updateConfig(key, request))
                .build();
    }
}
