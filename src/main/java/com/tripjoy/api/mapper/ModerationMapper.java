package com.tripjoy.api.mapper;

import org.springframework.stereotype.Component;

import com.tripjoy.api.dto.response.report.ModerationActionResponse;
import com.tripjoy.api.dto.response.simple.UserSimpleResponse;
import com.tripjoy.api.entity.ModerationAction;
import com.tripjoy.api.entity.User;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ModerationMapper {

    /**
     * Map ModerationAction entity to ModerationActionResponse DTO
     */
    public ModerationActionResponse toModerationActionResponse(ModerationAction action) {
        return ModerationActionResponse.builder()
                .id(action.getId())
                .moderatedUser(toUserSimpleResponse(action.getUser()))
                .admin(toUserSimpleResponse(action.getBa()))
                .actionType(action.getActionType())
                .createdAt(action.getCreatedAt())
                .note(action.getNote())
                .build();
    }

    /**
     * Map User entity to UserSimpleResponse
     */
    private UserSimpleResponse toUserSimpleResponse(User user) {
        return UserSimpleResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .avatarUrl(user.getAvatarUrl())
                .build();
    }
}
