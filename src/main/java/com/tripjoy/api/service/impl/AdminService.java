package com.tripjoy.api.service.impl;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tripjoy.api.dto.request.report.ModerationActionRequest;
import com.tripjoy.api.dto.response.report.ModerationActionResponse;
import com.tripjoy.api.entity.ModerationAction;
import com.tripjoy.api.entity.User;
import com.tripjoy.api.exception.AppException;
import com.tripjoy.api.exception.ErrorCode;
import com.tripjoy.api.mapper.UserMapper;
import com.tripjoy.api.repository.ModerationActionRepository;
import com.tripjoy.api.repository.UserRepository;
import com.tripjoy.api.service.IAdminService;
import com.tripjoy.api.utils.SecurityUtils;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminService implements IAdminService {

    UserRepository userRepository;
    ModerationActionRepository moderationActionRepository;
    UserMapper userMapper;

    @Override
    @Transactional
    public ModerationActionResponse moderateUser(ModerationActionRequest request) {
        User admin = getCurrentUser();
        User target = userRepository
                .findById(request.getUserId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        ModerationAction action = ModerationAction.builder()
                .actionType(request.getActionType().trim().toUpperCase())
                .note(request.getNote())
                .user(target)
                .ba(admin)
                .build();

        return toModerationActionResponse(moderationActionRepository.save(action));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ModerationActionResponse> getModerationActions(
            UUID userId, String actionType, UUID baId, Pageable pageable) {
        return moderationActionRepository
                .findByFilters(userId, actionType != null ? actionType.trim() : null, baId, pageable)
                .map(this::toModerationActionResponse);
    }

    private User getCurrentUser() {
        return userRepository
                .findById(SecurityUtils.getCurrentUserId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
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

