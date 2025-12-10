package com.tripjoy.api.service;

import com.tripjoy.api.dto.response.ConversationResponse;

import java.util.List;
import java.util.UUID;

public interface IConversationService {
    List<ConversationResponse> getUserConversations(UUID userId);

    ConversationResponse getConversationDetail(UUID conversationId, UUID currentUserId);
}
