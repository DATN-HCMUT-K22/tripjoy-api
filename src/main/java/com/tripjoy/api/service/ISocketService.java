package com.tripjoy.api.service;

import java.util.UUID;

import com.corundumstudio.socketio.SocketIOClient;
import com.tripjoy.api.dto.response.ChatMessageResponse;
import com.tripjoy.api.dto.response.ConversationResponse;

public interface ISocketService {
    void onConnect(SocketIOClient client);

    void onDisconnect(SocketIOClient client);

    void onJoinConversation(SocketIOClient client, String conversationId);

    void onLeaveConversation(SocketIOClient client, String conversationId);

    void onTyping(SocketIOClient client, String conversationId);

    void onStopTyping(SocketIOClient client, String conversationId);

    void sendNewMessage(UUID conversationId, ChatMessageResponse messageResponse);

    void sendLikeUpdate(UUID conversationId, UUID messageId, UUID userId, boolean isLiked);

    void sendPinUpdate(UUID conversationId, UUID messageId, UUID userId, boolean isPinned);

    void sendNotification(UUID userId, Object notification);

    /**
     * Notify a specific user that a new Direct Conversation has been created for them.
     *
     * <p>Sends the {@code new_conversation} Socket.IO event to the user's personal room
     * {@code user_{userId}}. The client should listen for this event and automatically
     * join the new conversation room ({@code join_conversation}) to start receiving messages.
     *
     * @param userId       the user to notify
     * @param conversation the new conversation payload (with name/avatar set from partner's perspective)
     */
    void notifyNewDirectConversation(UUID userId, ConversationResponse conversation);
}
