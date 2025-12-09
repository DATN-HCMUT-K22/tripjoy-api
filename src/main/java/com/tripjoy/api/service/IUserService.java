package com.tripjoy.api.service;

import com.tripjoy.api.dto.request.UserCreationRequest;
import com.tripjoy.api.dto.request.UserUpdateRequest;
import com.tripjoy.api.dto.response.UserResponse;

import java.util.List;
import java.util.UUID;

public interface IUserService {
    List<UserResponse> getUsers();

    UserResponse getUserById(UUID id);

    UserResponse getMyInfo();

    UserResponse createUser(UserCreationRequest request);

    UserResponse updateUser(UUID userId, UserUpdateRequest request);

    void deleteUser(UUID userId);
}
