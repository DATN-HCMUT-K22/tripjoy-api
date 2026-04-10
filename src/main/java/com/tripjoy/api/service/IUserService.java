package com.tripjoy.api.service;

import java.util.List;
import java.util.UUID;

import com.tripjoy.api.dto.request.UserCreationRequest;
import com.tripjoy.api.dto.request.ChangePasswordRequest;
import com.tripjoy.api.dto.request.UserProfileUpdateRequest;
import com.tripjoy.api.dto.request.UserRoleUpdateRequest;
import com.tripjoy.api.dto.response.UserPublicResponse;
import com.tripjoy.api.dto.response.UserResponse;
import com.tripjoy.api.dto.response.simple.UserSimpleResponse;

public interface IUserService {
    List<UserResponse> getUsers();

    /**
     * Luồng 2: Public profile — visible to any authenticated user.
     * Returns a limited DTO without sensitive fields (email, phone, credits, roles).
     */
    UserPublicResponse getPublicProfile(UUID id);

    /**
     * Luồng 3: Admin full view — requires ADMIN role.
     * Returns full UserResponse including sensitive fields.
     */
    UserResponse getUserDetailsForAdmin(UUID id);

    UserResponse getMyInfo();

    UserResponse createUser(UserCreationRequest request);

    UserResponse updateMyProfile(UserProfileUpdateRequest request);

    void changeMyPassword(ChangePasswordRequest request);

    UserResponse assignRoles(UUID userId, UserRoleUpdateRequest request);

    UserResponse updateUserStatus(UUID userId, boolean isLocked);

    void deleteUser(UUID userId);

    List<UserSimpleResponse> searchUsers(String keyword);
}
