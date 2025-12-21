package com.tripjoy.api.service;

import com.tripjoy.api.dto.request.chat.ChatMessageRequest;
import com.tripjoy.api.dto.response.ChatMessageResponse;

import java.util.List;
import java.util.UUID;

public interface IChatMessageService {
    void likeMessage(UUID messageId, UUID userId);

    void unlikeMessage(UUID messageId, UUID userId);

    ChatMessageResponse sendMessage(UUID conversationId, UUID senderId, ChatMessageRequest request);

    void pinMessage(UUID conversationId, UUID messageId, UUID userId);

    void unpinMessage(UUID conversationId, UUID messageId, UUID userId);

    List<ChatMessageResponse> getPinnedMessages(UUID conversationId, UUID userId);
}
