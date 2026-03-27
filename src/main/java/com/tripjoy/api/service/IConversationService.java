package com.tripjoy.api.service;

import java.util.List;
import java.util.UUID;

import com.tripjoy.api.dto.request.chat.ConversationUpdateRequest;
import com.tripjoy.api.dto.response.ConversationResponse;

public interface IConversationService {
    List<ConversationResponse> getUserConversations(UUID userId);

    ConversationResponse getConversationDetail(UUID conversationId, UUID currentUserId);

    ConversationResponse updateConversation(UUID conversationId, ConversationUpdateRequest request, UUID currentUserId);
}
