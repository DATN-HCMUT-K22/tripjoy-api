package com.tripjoy.api.service;

import java.util.List;
import java.util.UUID;

import com.tripjoy.api.dto.request.UserCreationRequest;
import com.tripjoy.api.dto.request.ChangePasswordRequest;
import com.tripjoy.api.dto.request.UserProfileUpdateRequest;
import com.tripjoy.api.dto.request.UserRoleUpdateRequest;
import com.tripjoy.api.dto.response.UserResponse;
import com.tripjoy.api.dto.response.simple.UserSimpleResponse;

public interface IUserService {
    List<UserResponse> getUsers();

    UserResponse getUserById(UUID id);

    UserResponse getMyInfo();

    UserResponse createUser(UserCreationRequest request);

    UserResponse updateMyProfile(UserProfileUpdateRequest request);

    void changeMyPassword(ChangePasswordRequest request);

    UserResponse assignRoles(UUID userId, UserRoleUpdateRequest request);

    UserResponse updateUserStatus(UUID userId, boolean isLocked);

    void deleteUser(UUID userId);

    List<UserSimpleResponse> searchUsers(String keyword);
}
