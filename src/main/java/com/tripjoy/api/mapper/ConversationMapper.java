package com.tripjoy.api.mapper;

import java.util.UUID;

import org.mapstruct.*;

import com.tripjoy.api.configuration.mapper.BaseMapperConfig;
import com.tripjoy.api.dto.response.ConversationResponse;
import com.tripjoy.api.entity.Conversation;

@Mapper(
        config = BaseMapperConfig.class,
        uses = {UserMapper.class, ChatMessageMapper.class})
public interface ConversationMapper {

    @Mapping(target = "name", ignore = true)
    @Mapping(target = "avatar", ignore = true)
    @Mapping(target = "groupId", source = "group.id")
    @Mapping(target = "members", ignore = true)
    @Mapping(target = "unreadCount", ignore = true)
    @Mapping(target = "isPinned", ignore = true)
    ConversationResponse toResponse(Conversation conversation, @Context UUID currentUserId);
}
