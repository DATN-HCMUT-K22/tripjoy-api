package com.tripjoy.api.service.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tripjoy.api.configuration.redis.RedisCacheConfig;
import com.tripjoy.api.dto.request.AdminUserCreationRequest;
import com.tripjoy.api.dto.request.ChangePasswordRequest;
import com.tripjoy.api.dto.request.UserCreationRequest;
import com.tripjoy.api.dto.request.UserProfileUpdateRequest;
import com.tripjoy.api.dto.request.UserRoleUpdateRequest;
import com.tripjoy.api.dto.response.UserPublicResponse;
import com.tripjoy.api.dto.response.UserResponse;
import com.tripjoy.api.dto.response.report.ModerationActionResponse;
import com.tripjoy.api.dto.response.simple.UserSimpleResponse;
import com.tripjoy.api.entity.ModerationAction;
import com.tripjoy.api.entity.Role;
import com.tripjoy.api.entity.User;
import com.tripjoy.api.exception.AppException;
import com.tripjoy.api.exception.ErrorCode;
import com.tripjoy.api.mapper.UserMapper;
import com.tripjoy.api.repository.ModerationActionRepository;
import com.tripjoy.api.repository.RoleRepository;
import com.tripjoy.api.repository.UserRepository;
import com.tripjoy.api.service.IUserService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

/**
 * User profile service implementing 3 distinct access flows:
 *
 * <h3>Flow 1 — My Profile ({@link #getMyInfo})</h3>
 * <ul>
 *   <li>Caller: current authenticated user only</li>
 *   <li>Data: full UserResponse including credits, email, phone</li>
 *   <li>Cache: <b>NO</b> — data changes frequently (credits, avatar)</li>
 * </ul>
 *
 * <h3>Flow 2 — Public Profile ({@link #getPublicProfile})</h3>
 * <ul>
 *   <li>Caller: any authenticated user looking at another user's profile</li>
 *   <li>Data: {@link UserPublicResponse} — <b>no</b> sensitive fields (email, phone, credits, roles)</li>
 *   <li>Cache: {@code user:public} — 12h TTL, safe to share across callers</li>
 * </ul>
 *
 * <h3>Flow 3 — System Admin View ({@link #getUserDetailsForAdmin})</h3>
 * <ul>
 *   <li>Caller: SYSTEM_ADMIN role only — enforced by {@code @PreAuthorize} <i>before</i> execution</li>
 *   <li>Data: full UserResponse</li>
 *   <li>Cache: {@code user:admin} — 12h TTL, separate namespace from public cache</li>
 * </ul>
 *
 * <h3>Why NOT @PostAuthorize + @Cacheable?</h3>
 * <p>{@code @PostAuthorize} checks AFTER the method runs and AFTER the cache is populated.
 * If user A's data is cached via {@code getUserById(aliceId)}, and Admin then calls the same
 * method, the cache returns alice's data and {@code @PostAuthorize} sees
 * {@code alice.username != admin.username} → throws 403. This breaks the cache for everyone
 * except the data owner. The 3-flow pattern avoids this entirely by using {@code @PreAuthorize}
 * (admin-only, blocks before execution) or no auth restriction at all (public profile, data
 * is non-sensitive by design).
 */
@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserService implements IUserService {

    UserRepository userRepository;
    RoleRepository roleRepository;
    ModerationActionRepository moderationActionRepository;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;

    // ==================== Flow 1: My Profile ====================

    /**
     * Get current user's own full profile.
     * NOT cached — credits, avatar, and other fields can change at any time within the session.
     */
    @Override
    public UserResponse getMyInfo() {
        String currentUserId =
                SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository
                .findById(UUID.fromString(currentUserId))
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        return userMapper.toUserResponse(user);
    }

    // ==================== Flow 2: Public Profile ====================

    /**
     * Get a non-sensitive public profile of any user.
     * Cached in {@code user:public} for 12 hours.
     * Response intentionally omits email, phone, credits, isLocked, and roles.
     */
    @Override
    @Cacheable(value = RedisCacheConfig.CACHE_USER_PUBLIC, key = "#id")
    public UserPublicResponse getPublicProfile(UUID id) {
        log.debug("Cache MISS — loading public profile from DB: {}", id);
        User user = userRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        return userMapper.toPublicResponse(user);
    }

    // ==================== Flow 3: System Admin View ====================

    /**
     * Get full user details — SYSTEM_ADMIN only.
     * {@code @PreAuthorize} blocks non-admins BEFORE execution (and before cache is read),
     * so admins and non-admins never share the same cache entry.
     * Cached in {@code user:admin} for 12 hours under a separate namespace from public profiles.
     */
    @Override
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Cacheable(value = RedisCacheConfig.CACHE_USER_ADMIN_VIEW, key = "#id")
    public UserResponse getUserDetailsForAdmin(UUID id) {
        log.debug("Cache MISS — loading full user details for admin: {}", id);
        return userMapper.toUserResponse(
                userRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND)));
    }

    // ==================== Admin Mutations ====================

    @Override
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','BUSINESS_ADMIN')")
    public Page<UserResponse> getUsers(Pageable pageable, String q) {
        if (q != null && !q.trim().isBlank()) {
            return userRepository
                    .searchByUsernameOrEmailPaged(q.trim(), pageable)
                    .map(userMapper::toUserResponse);
        }
        return userRepository.findAll(pageable).map(userMapper::toUserResponse);
    }

    public UserResponse createUser(UserCreationRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) throw new AppException(ErrorCode.USER_EXISTED);
        User user = userMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userMapper.toUserResponse(userRepository.save(user));
    }

    @Override
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Transactional
    public UserResponse createUserWithRoles(AdminUserCreationRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) throw new AppException(ErrorCode.USER_EXISTED);
        if (userRepository.existsByEmail(request.getEmail())) throw new AppException(ErrorCode.EMAIL_EXISTED);

        List<Role> roles = roleRepository.findAllById(request.getRoles());
        Set<String> foundRoleNames = roles.stream().map(Role::getName).collect(Collectors.toSet());
        if (foundRoleNames.size() != request.getRoles().size() || !foundRoleNames.containsAll(request.getRoles())) {
            throw new AppException(ErrorCode.ROLE_NOT_FOUND);
        }

        User user = userMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRoles(new HashSet<>(roles));

        return userMapper.toUserResponse(userRepository.save(user));
    }

    /**
     * Update own profile and evict BOTH caches (public + admin view) for the user.
     * Key for eviction comes from the saved entity's ID (resolved via SpEL on return value).
     */
    @Override
    @Caching(
            evict = {
                @CacheEvict(value = RedisCacheConfig.CACHE_USER_PUBLIC, key = "#result.id"),
                @CacheEvict(value = RedisCacheConfig.CACHE_USER_ADMIN_VIEW, key = "#result.id")
            })
    public UserResponse updateMyProfile(UserProfileUpdateRequest request) {
        String currentUserId =
                SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository
                .findById(UUID.fromString(currentUserId))
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        userMapper.updateMyProfile(user, request);
        return userMapper.toUserResponse(userRepository.save(user));
    }

    @Override
    public void changeMyPassword(ChangePasswordRequest request) {
        String currentUserId =
                SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository
                .findById(UUID.fromString(currentUserId))
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Caching(
            evict = {
                @CacheEvict(value = RedisCacheConfig.CACHE_USER_PUBLIC, key = "#userId"),
                @CacheEvict(value = RedisCacheConfig.CACHE_USER_ADMIN_VIEW, key = "#userId")
            })
    public UserResponse assignRoles(UUID userId, UserRoleUpdateRequest request) {
        User user = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        List<Role> roles = roleRepository.findAllById(request.getRoles());
        user.setRoles(new HashSet<>(roles));
        return userMapper.toUserResponse(userRepository.save(user));
    }

    @Override
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Caching(
            evict = {
                @CacheEvict(value = RedisCacheConfig.CACHE_USER_PUBLIC, key = "#userId"),
                @CacheEvict(value = RedisCacheConfig.CACHE_USER_ADMIN_VIEW, key = "#userId")
            })
    public UserResponse updateUserStatus(UUID userId, boolean isLocked) {
        User user = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        user.setIsLocked(isLocked);
        return userMapper.toUserResponse(userRepository.save(user));
    }

    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Caching(
            evict = {
                @CacheEvict(value = RedisCacheConfig.CACHE_USER_PUBLIC, key = "#userId"),
                @CacheEvict(value = RedisCacheConfig.CACHE_USER_ADMIN_VIEW, key = "#userId")
            })
    public void deleteUser(UUID userId) {
        userRepository.deleteById(userId);
    }

    @Override
    public List<UserSimpleResponse> searchUsers(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) return List.of();
        return userRepository.searchByUsernameOrEmail(keyword.trim()).stream()
                .map(userMapper::toUserSimpleResponse)
                // Use collect(Collectors.toList()) instead of .toList() to ensure the result is a mutable ArrayList.
                // Immutable collections from .toList() lack a default constructor, causing Jackson deserialization
                // failures in Redis.
                .collect(Collectors.toList());
    }

    @Override
    public Page<UserSimpleResponse> searchUsersGlobal(String keyword, Pageable pageable) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return Page.empty();
        }
        return userRepository.searchGlobalUsers(keyword.trim(), pageable).map(userMapper::toUserSimpleResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ModerationActionResponse> getMyModerationHistory(Pageable pageable) {
        String currentUserId =
                SecurityContextHolder.getContext().getAuthentication().getName();
        return moderationActionRepository
                .findByFilters(UUID.fromString(currentUserId), null, null, pageable)
                .map(this::toModerationActionResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ModerationActionResponse> getUserModerationHistory(UUID userId, Pageable pageable) {
        if (!userRepository.existsById(userId)) {
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }
        return moderationActionRepository
                .findByFilters(userId, null, null, pageable)
                .map(this::toModerationActionResponse);
    }

    private ModerationActionResponse toModerationActionResponse(ModerationAction action) {
        return ModerationActionResponse.builder()
                .id(action.getId())
                .moderatedUser(userMapper.toUserSimpleResponse(action.getUser()))
                .admin(userMapper.toUserSimpleResponse(action.getBa()))
                .actionType(action.getActionType())
                .createdAt(action.getCreatedAt())
                .note(action.getNote())
                .build();
    }
}

