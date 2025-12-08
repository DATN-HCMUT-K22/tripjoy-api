package com.tripjoy.api.mapper;

import com.tripjoy.api.configuration.mapper.BaseMapperConfig;
import com.tripjoy.api.dto.response.ConversationResponse;
import com.tripjoy.api.entity.Conversation;
import com.tripjoy.api.entity.ConversationMember;
import com.tripjoy.api.entity.User;
import com.tripjoy.api.enums.ConversationType;
import org.mapstruct.*;

import java.util.UUID;

@Mapper(config = BaseMapperConfig.class, uses = {UserMapper.class, ChatMessageMapper.class})
public interface ConversationMapper {

    // 1. Khai báo hàm map chính, nhận thêm tham số context là currentUserId
    @Mapping(target = "name", ignore = true)   // Sẽ map thủ công bên dưới
    @Mapping(target = "avatar", ignore = true) // Sẽ map thủ công bên dưới
    @Mapping(target = "groupId", source = "group.id") // Map group id
    @Mapping(target = "members", source = "members")  // Map list members
    ConversationResponse toResponse(Conversation conversation, @Context UUID currentUserId);

    // 2. Logic tùy chỉnh sau khi map xong các field cơ bản
    @AfterMapping
    default void mapDynamicInfo(@MappingTarget ConversationResponse response,
                                Conversation conversation,
                                @Context UUID currentUserId) {

        if (conversation.getType() == ConversationType.GROUP) {
            // CASE 1: GROUP CHAT -> Lấy thông tin từ Group
            if (conversation.getGroup() != null) {
                response.setName(conversation.getGroup().getName());
                response.setAvatar(conversation.getGroup().getAvatar());
                // Nếu conversation có tên riêng (vd đặt lại tên nhóm chat) thì ưu tiên dùng
                if (conversation.getName() != null) {
                    response.setName(conversation.getName());
                }
            }
        } else {
            // CASE 2: DIRECT CHAT -> Lấy thông tin người KIA (không phải mình)
            User partner = conversation.getMembers().stream()
                    .map(ConversationMember::getUser)
                    .filter(user -> !user.getId().equals(currentUserId)) // Lọc bỏ chính mình
                    .findFirst()
                    .orElse(null);

            if (partner != null) {
                response.setName(partner.getFullName());
                response.setAvatar(partner.getAvatarUrl());
            } else {
                // Trường hợp chat với chính mình (Saved Messages) hoặc lỗi data
                response.setName("Unknown User");
            }
        }

        // 3. Map thêm thông tin Unread Count & Pinned cho riêng User đó
        ConversationMember myMemberInfo = conversation.getMembers().stream()
                .filter(m -> m.getUser().getId().equals(currentUserId))
                .findFirst()
                .orElse(null);

        if (myMemberInfo != null) {
            response.setUnreadCount(myMemberInfo.getUnreadCount());
            response.setIsPinned(myMemberInfo.getIsPinned());
        }
    }
}