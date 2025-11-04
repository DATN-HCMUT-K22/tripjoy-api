package com.tripjoy.api.controller;

import com.tripjoy.api.constant.Endpoint;
import com.tripjoy.api.dto.request.PermissionRequest;
import com.tripjoy.api.dto.request.RoleRequest;
import com.tripjoy.api.dto.response.ApiResponse;
import com.tripjoy.api.dto.response.PermissionResponse;
import com.tripjoy.api.dto.response.RoleResponse;
import com.tripjoy.api.service.PermissionService;
import com.tripjoy.api.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(Endpoint.Role.BASE)
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Role", description = "Endpoints for managing roles (Admin only)")
@PreAuthorize("hasRole('ADMIN')")
public class RoleController {
    RoleService roleService;

    @Operation(summary = "Create a new role")
    @PostMapping
    public ApiResponse<RoleResponse> create(@RequestBody RoleRequest request) {
        return ApiResponse.<RoleResponse>builder()
                .data(roleService.create(request))
                .build();
    }

    @Operation(summary = "Get all roles")
    @GetMapping
    public ApiResponse<List<RoleResponse>> getAll() {
        return ApiResponse.<List<RoleResponse>>builder()
                .data(roleService.getAll())
                .build();
    }

    @Operation(summary = "Delete a role")
    @DeleteMapping(Endpoint.Role.ID)
    public ApiResponse<Void> delete(@PathVariable String roleId) {
        roleService.delete(roleId);
        return ApiResponse.<Void>builder()
                .message("Role has been deleted")
                .build();
    }

}
