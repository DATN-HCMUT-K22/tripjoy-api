package com.tripjoy.api.service;

import java.util.List;
import java.util.UUID;

import com.tripjoy.api.dto.request.chat.ConversationUpdateRequest;
import com.tripjoy.api.dto.response.ConversationResponse;

public interface IConversationService {
    List<ConversationResponse> getUserConversations(UUID userId);

    /**
     * Create or retrieve an existing Direct Message (1-on-1) conversation.
     * Idempotent: if a DIRECT conversation already exists between the two users,
     * the existing conversation is returned without creating a duplicate.
     *
     * @param initiatorId the user initiating the DM
     * @param targetUserId the user to start a conversation with
     * @return ConversationResponse (new or existing)
     */
    ConversationResponse createDirectConversation(UUID initiatorId, UUID targetUserId);

    ConversationResponse getConversationDetail(UUID conversationId, UUID currentUserId);

    ConversationResponse updateConversation(UUID conversationId, ConversationUpdateRequest request, UUID currentUserId);

    void resetUnreadCount(UUID conversationId, UUID currentUserId);
}
