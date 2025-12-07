package com.tripjoy.api.controller;

import com.tripjoy.api.constant.Endpoint;
import com.tripjoy.api.dto.request.UserCreationRequest;
import com.tripjoy.api.dto.request.UserUpdateRequest;
import com.tripjoy.api.dto.response.ApiResponse;
import com.tripjoy.api.dto.response.UserResponse;
import com.tripjoy.api.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(Endpoint.User.BASE)
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Users", description = "Endpoints for managing users accounts")
public class UserController {
    UserService userService;

    @GetMapping
    @Operation(summary = "Get all users", description = "Retrieves a list of all registered users.")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
//    @PreAuthorize("hasAuthority('APPROVE_POST')")
    public ApiResponse<List<UserResponse>> getUsers() {

        return ApiResponse.<List<UserResponse>>builder()
                .data(userService.getUsers())
                .build();
    }

    @GetMapping(Endpoint.User.ME)
    @Operation(summary = "Get current users's info", description = "Retrieves the profile information of the currently authenticated users.")
    public ApiResponse<UserResponse> getMyInfo() {
        return ApiResponse.<UserResponse>builder()
                .data(userService.getMyInfo())
                .build();
    }

    @Operation(summary = "Create a new users", description = "Creates a new users account based on the provided request.")
    @PostMapping
    public ApiResponse<UserResponse> createUser(@RequestBody @Valid UserCreationRequest request) {
        return ApiResponse.<UserResponse>builder()
                .data(userService.createUser(request))
                .build();
    }

    @Operation(summary = "Update users by ID", description = "Updates an existing users's information by their unique ID.")
    @PutMapping(Endpoint.User.ID)
    ApiResponse<UserResponse> updateUser(@PathVariable UUID userId, @RequestBody UserUpdateRequest request) {
        return ApiResponse.<UserResponse>builder()
                .data(userService.updateUser(userId, request))
                .build();
    }

    @Operation(summary = "Delete users by ID", description = "Deletes a users account from the system by their unique ID.")
    @DeleteMapping(Endpoint.User.ID)
    ApiResponse<Void> deleteUser(@PathVariable UUID userId) {
        userService.deleteUser(userId);
        return ApiResponse.<Void>builder()
                .message("Users has been deleted")
                .build();
    }
}
