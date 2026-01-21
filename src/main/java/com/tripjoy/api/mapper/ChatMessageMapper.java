package com.tripjoy.api.mapper;

import com.tripjoy.api.configuration.mapper.BaseMapperConfig;
import com.tripjoy.api.dto.request.chat.ChatMessageRequest;
import com.tripjoy.api.dto.response.ChatMessageResponse;
import com.tripjoy.api.entity.ChatMessage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = BaseMapperConfig.class, uses = { UserMapper.class }) // uses: Để tái sử dụng UserMapper
public interface ChatMessageMapper {

    // 1. Request -> Entity
    ChatMessage toEntity(ChatMessageRequest request);

    // 2. Entity -> Response
    @Mapping(source = "conversation.id", target = "conversationId") // Đã đổi group -> conversation theo DB mới

    @Mapping(source = "sender.id", target = "senderId")
    @Mapping(source = "sender", target = "sender")

    @Mapping(source = "parentMessage.id", target = "parentMessageId")
    @Mapping(source = "parentMessage", target = "parentMessage")

    // Like information mappings
    @Mapping(source = "isPinned", target = "isPinned")
    @Mapping(target = "likeCount", expression = "java(chatMessage.getLikeUsers().size())")
    @Mapping(target = "isLikedByCurrentUser", ignore = true) // Set manually in service

    ChatMessageResponse toResponse(ChatMessage chatMessage);
}