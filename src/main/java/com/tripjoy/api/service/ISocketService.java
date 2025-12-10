package com.tripjoy.api.service;

import com.corundumstudio.socketio.SocketIOClient;
import com.tripjoy.api.dto.response.ChatMessageResponse;

import java.util.UUID;

public interface ISocketService {
    void onConnect(SocketIOClient client);

    void onDisconnect(SocketIOClient client);

    void onJoinConversation(SocketIOClient client, String conversationId);

    void onLeaveConversation(SocketIOClient client, String conversationId);

    void onTyping(SocketIOClient client, String conversationId);

    void onStopTyping(SocketIOClient client, String conversationId);

    void sendNewMessage(UUID conversationId, ChatMessageResponse messageResponse);

    void sendLikeUpdate(UUID conversationId, UUID messageId, UUID userId, boolean isLiked);
}
