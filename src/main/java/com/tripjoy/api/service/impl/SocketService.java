package com.tripjoy.api.service.impl;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.annotation.OnConnect;
import com.corundumstudio.socketio.annotation.OnDisconnect;
import com.corundumstudio.socketio.annotation.OnEvent;
import com.tripjoy.api.configuration.socketio.SocketRateLimiter;
import com.tripjoy.api.dto.response.ChatMessageResponse;
import com.tripjoy.api.service.ISocketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class SocketService implements ISocketService {

    private final SocketIOServer server;
    private final SocketRateLimiter rateLimiter;

    @OnConnect
    public void onConnect(SocketIOClient client) {
        String userId = client.getHandshakeData().getSingleUrlParam("userId");
        String sessionId = client.getSessionId().toString();

        if (userId != null && !userId.trim().isEmpty()) {
            String userRoom = "user_" + userId;
            client.joinRoom(userRoom);
            log.info("User connected: userId={}, sessionId={}", userId, sessionId);
        } else {
            log.warn("Client connected without userId: sessionId={}", sessionId);
        }
    }

    @OnDisconnect
    public void onDisconnect(SocketIOClient client) {
        String userId = client.getHandshakeData().getSingleUrlParam("userId");
        String sessionId = client.getSessionId().toString();

        if (userId != null) {
            rateLimiter.cleanup(userId);
        }

        log.info("Client disconnected: userId={}, sessionId={}", userId != null ? userId : "unknown", sessionId);
    }

    @OnEvent("join_conversation")
    public void onJoinConversation(SocketIOClient client, String conversationId) {
        try {
            String roomName = "conversation_" + conversationId;
            client.joinRoom(roomName);

            String userId = client.getHandshakeData().getSingleUrlParam("userId");
            log.info("Client joined conversation: userId={}, conversationId={}", userId, conversationId);
        } catch (Exception e) {
            log.error("Error joining conversation {}: {}", conversationId, e.getMessage());
        }
    }

    @OnEvent("leave_conversation")
    public void onLeaveConversation(SocketIOClient client, String conversationId) {
        try {
            String roomName = "conversation_" + conversationId;
            client.leaveRoom(roomName);

            String userId = client.getHandshakeData().getSingleUrlParam("userId");
            log.info("Client left conversation: userId={}, conversationId={}", userId, conversationId);
        } catch (Exception e) {
            log.error("Error leaving conversation {}: {}", conversationId, e.getMessage());
        }
    }

    @OnEvent("typing")
    public void onTyping(SocketIOClient client, String conversationId) {
        try {
            String userId = client.getHandshakeData().getSingleUrlParam("userId");

            if (!rateLimiter.allowTyping(userId)) {
                return; // Silently ignore if rate limited
            }

            String roomName = "conversation_" + conversationId;

            server.getRoomOperations(roomName)
                    .getClients()
                    .stream()
                    .filter(c -> !c.getSessionId().equals(client.getSessionId()))
                    .forEach(c -> c.sendEvent("user_typing", userId));

        } catch (Exception e) {
            log.error("Error broadcasting typing indicator: {}", e.getMessage());
        }
    }

    @OnEvent("stop_typing")
    public void onStopTyping(SocketIOClient client, String conversationId) {
        try {
            String userId = client.getHandshakeData().getSingleUrlParam("userId");
            String roomName = "conversation_" + conversationId;

            server.getRoomOperations(roomName)
                    .getClients()
                    .stream()
                    .filter(c -> !c.getSessionId().equals(client.getSessionId()))
                    .forEach(c -> c.sendEvent("user_stop_typing", userId));

        } catch (Exception e) {
            log.error("Error broadcasting stop typing: {}", e.getMessage());
        }
    }

    public void sendNewMessage(UUID conversationId, ChatMessageResponse messageResponse) {
        try {
            String roomName = "conversation_" + conversationId;
            server.getRoomOperations(roomName).sendEvent("receive_message", messageResponse);
            log.info("Message broadcasted: messageId={}, conversationId={}", messageResponse.getId(), conversationId);
        } catch (Exception e) {
            log.error("Failed to broadcast message: conversationId={}, messageId={}", conversationId,
                    messageResponse.getId(), e);
        }
    }

    public void sendLikeUpdate(UUID conversationId, UUID messageId, UUID userId, boolean isLiked) {
        try {
            String roomName = "conversation_" + conversationId;
            server.getRoomOperations(roomName).sendEvent("update_like", messageId, userId, isLiked);
        } catch (Exception e) {
            log.error("Failed to broadcast like update: {}", e.getMessage());
        }
    }
}
