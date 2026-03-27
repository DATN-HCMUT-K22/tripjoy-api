package com.tripjoy.api.controller;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;

import com.tripjoy.api.constant.Endpoint;
import com.tripjoy.api.dto.request.UserCreationRequest;
import com.tripjoy.api.dto.request.UserUpdateRequest;
import com.tripjoy.api.dto.response.ApiResponse;
import com.tripjoy.api.dto.response.UserResponse;
import com.tripjoy.api.dto.response.simple.UserSimpleResponse;
import com.tripjoy.api.service.IUserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping(Endpoint.User.BASE)
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Users", description = "Endpoints for managing user accounts")
public class UserController {
    IUserService userService;

    @Operation(
            summary = "Search users by username or email",
            description = "Searches for users whose username or email contains the given keyword. "
                    + "Uses case-insensitive LIKE matching.")
    @GetMapping(Endpoint.User.SEARCH)
    public ApiResponse<List<UserSimpleResponse>> searchUsers(@RequestParam String q) {
        return ApiResponse.<List<UserSimpleResponse>>builder()
                .data(userService.searchUsers(q))
                .build();
    }

    @GetMapping
    @Operation(summary = "Get all users", description = "Retrieves a list of all registered users.")
    //    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    // @PreAuthorize("hasAuthority('APPROVE_POST')")
    public ApiResponse<List<UserResponse>> getUsers() {

        return ApiResponse.<List<UserResponse>>builder()
                .data(userService.getUsers())
                .build();
    }

    @GetMapping(Endpoint.User.ME)
    @Operation(
            summary = "Get current user's info",
            description = "Retrieves the profile information of the currently authenticated user.")
    public ApiResponse<UserResponse> getMyInfo() {
        return ApiResponse.<UserResponse>builder().data(userService.getMyInfo()).build();
    }

    @Operation(summary = "Create a new user", description = "Creates a new user account based on the provided request.")
    @PostMapping
    public ApiResponse<UserResponse> createUser(@RequestBody @Valid UserCreationRequest request) {
        return ApiResponse.<UserResponse>builder()
                .data(userService.createUser(request))
                .build();
    }

    @Operation(
            summary = "Update user by ID",
            description = "Updates an existing user's information by their unique ID.")
    @PutMapping(Endpoint.User.ID)
    ApiResponse<UserResponse> updateUser(@PathVariable UUID userId, @RequestBody UserUpdateRequest request) {
        return ApiResponse.<UserResponse>builder()
                .data(userService.updateUser(userId, request))
                .build();
    }

    @Operation(
            summary = "Delete user by ID",
            description = "Deletes a user account from the system by their unique ID.")
    @DeleteMapping(Endpoint.User.ID)
    ApiResponse<Void> deleteUser(@PathVariable UUID userId) {
        userService.deleteUser(userId);
        return ApiResponse.<Void>builder().message("User has been deleted").build();
    }
}
