package com.tripjoy.api.service;

import com.tripjoy.api.dto.request.chat.ChatMessageRequest;
import com.tripjoy.api.dto.response.ChatMessageResponse;
import com.tripjoy.api.dto.response.MessageCursorResponse;
import com.tripjoy.api.dto.response.simple.UserSimpleResponse;

import java.util.List;
import java.util.UUID;

public interface IChatMessageService {
    void likeMessage(UUID messageId, UUID userId);

    void unlikeMessage(UUID messageId, UUID userId);

    ChatMessageResponse sendMessage(UUID conversationId, UUID senderId, ChatMessageRequest request);

    void pinMessage(UUID conversationId, UUID messageId, UUID userId);

    void unpinMessage(UUID conversationId, UUID messageId, UUID userId);

    List<ChatMessageResponse> getPinnedMessages(UUID conversationId, UUID userId);

    MessageCursorResponse getMessages(
            UUID conversationId,
            UUID currentUserId,
            String before,
            String after,
            Integer limit);

    List<UserSimpleResponse> getMessageLikes(UUID messageId, UUID currentUserId);
}
