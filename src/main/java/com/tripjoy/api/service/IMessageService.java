package com.tripjoy.api.service;

import com.tripjoy.api.dto.request.chat.ChatMessageRequest;
import com.tripjoy.api.dto.response.ChatMessageResponse;

import java.util.UUID;

public interface IMessageService {
    void toggleLikeMessage(UUID messageId, UUID userId);

    ChatMessageResponse sendMessage(UUID conversationId, UUID senderId, ChatMessageRequest request);
}
