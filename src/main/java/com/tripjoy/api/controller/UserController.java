package com.tripjoy.api.controller;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import com.tripjoy.api.constant.Endpoint;
import com.tripjoy.api.dto.request.ChangePasswordRequest;
import com.tripjoy.api.dto.request.UserCreationRequest;
import com.tripjoy.api.dto.request.UserProfileUpdateRequest;
import com.tripjoy.api.dto.request.UserRoleUpdateRequest;
import com.tripjoy.api.dto.request.UserStatusUpdateRequest;
import com.tripjoy.api.dto.response.ApiResponse;
import com.tripjoy.api.dto.response.UserPublicResponse;
import com.tripjoy.api.dto.response.UserResponse;
import com.tripjoy.api.dto.response.simple.UserSimpleResponse;
import com.tripjoy.api.service.IUserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
                summary = "Get users (Admin only)",
                description = """
                        Returns a paginated list of all users. Supports optional keyword filter on
                        username or email (case-insensitive LIKE).
                        
                        **No `q` param** → returns all users (paginated).
                        **With `?q=keyword`** → filters by username OR email.
                        
                        Requires `ADMIN` role.
                        """)
        @GetMapping
        public ApiResponse<Page<UserResponse>> getUsers(
                @Parameter(description = "Optional keyword filter on username or email", example = "nguyen")
                @RequestParam(required = false) String q,
                Pageable pageable) {
                return ApiResponse.<Page<UserResponse>>builder()
                                .data(userService.getUsers(pageable, q))
                                .build();
        }

        @GetMapping(Endpoint.User.ME)
        @Operation(summary = "Get current user's info", description = "Retrieves the profile information of the currently authenticated user. Full data including credits and email.")
        public ApiResponse<UserResponse> getMyInfo() {
                return ApiResponse.<UserResponse>builder().data(userService.getMyInfo()).build();
        }

        @GetMapping(Endpoint.User.ID + "/profile")
        @Operation(summary = "Get user's public profile", description = "Returns a public, non-sensitive profile of any user (avatar, bio, fullName). Cached 12h.")
        public ApiResponse<UserPublicResponse> getPublicProfile(@PathVariable("userId") UUID userId) {
                return ApiResponse.<UserPublicResponse>builder()
                                .data(userService.getPublicProfile(userId))
                                .build();
        }

        @GetMapping(Endpoint.User.ID + "/admin-view")
        @Operation(summary = "Admin: Get full user details", description = "Returns complete user data including sensitive fields. Requires ADMIN role. Cached 12h (admin namespace).")
        public ApiResponse<UserResponse> getUserDetailsForAdmin(@PathVariable("userId") UUID userId) {
                return ApiResponse.<UserResponse>builder()
                                .data(userService.getUserDetailsForAdmin(userId))
                                .build();
        }

        @Operation(summary = "Create a new user", description = "Creates a new user account based on the provided request.")
        @PostMapping
        public ApiResponse<UserResponse> createUser(@RequestBody @Valid UserCreationRequest request) {
                return ApiResponse.<UserResponse>builder()
                                .data(userService.createUser(request))
                                .build();
        }

        @Operation(summary = "Update my profile", description = "Updates the authenticated user's profile information.")
        @PatchMapping(Endpoint.User.ME)
        ApiResponse<UserResponse> updateMyProfile(@RequestBody @Valid UserProfileUpdateRequest request) {
                return ApiResponse.<UserResponse>builder()
                                .data(userService.updateMyProfile(request))
                                .build();
        }

        @Operation(summary = "Change my password", description = "Changes the authenticated user's password securely.")
        @PutMapping(Endpoint.User.ME_PASSWORD)
        ApiResponse<Void> changeMyPassword(@RequestBody @Valid ChangePasswordRequest request) {
                userService.changeMyPassword(request);
                return ApiResponse.<Void>builder().message("Password changed successfully").build();
        }

        @Operation(summary = "Assign roles to user (Admin only)", description = "Assigns a set of roles to a specific user.")
        @PutMapping(Endpoint.User.ID_ROLES)
        ApiResponse<UserResponse> assignRoles(
                        @PathVariable("userId") UUID userId, @RequestBody @Valid UserRoleUpdateRequest request) {
                return ApiResponse.<UserResponse>builder()
                                .data(userService.assignRoles(userId, request))
                                .build();
        }

        @Operation(summary = "Update user locked status (Admin only)", description = "Locks or unlocks a user account.")
        @PatchMapping(Endpoint.User.ID_STATUS)
        ApiResponse<UserResponse> updateUserStatus(
                        @PathVariable("userId") UUID userId, @RequestBody @Valid UserStatusUpdateRequest request) {
                return ApiResponse.<UserResponse>builder()
                                .data(userService.updateUserStatus(userId, request.getIsLocked()))
                                .build();
        }

        @Operation(summary = "Delete user by ID", description = "Deletes a user account from the system by their unique ID.")
        @DeleteMapping(Endpoint.User.ID)
        ApiResponse<Void> deleteUser(@PathVariable("userId") UUID userId) {
                userService.deleteUser(userId);
                return ApiResponse.<Void>builder().message("User has been deleted").build();
        }
}
