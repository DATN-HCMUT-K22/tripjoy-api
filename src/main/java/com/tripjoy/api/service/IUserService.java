package com.tripjoy.api.service;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.tripjoy.api.dto.request.UserCreationRequest;
import com.tripjoy.api.dto.request.ChangePasswordRequest;
import com.tripjoy.api.dto.request.UserProfileUpdateRequest;
import com.tripjoy.api.dto.request.UserRoleUpdateRequest;
import com.tripjoy.api.dto.response.UserPublicResponse;
import com.tripjoy.api.dto.response.UserResponse;
import com.tripjoy.api.dto.response.simple.UserSimpleResponse;

public interface IUserService {

    /**
     * Admin-only paginated user list with optional keyword filter.
     * Replaces the old unpaginated {@code getUsers()} to prevent OOM on large datasets.
     *
     * @param pageable pagination and sort (Spring standard)
     * @param q        optional keyword — filters by username OR email (LIKE, case-insensitive)
     */
    Page<UserResponse> getUsers(Pageable pageable, String q);

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

    Page<UserSimpleResponse> searchUsersGlobal(String keyword, Pageable pageable);
}
