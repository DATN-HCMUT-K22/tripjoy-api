package com.tripjoy.api.service.impl;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.annotation.OnConnect;
import com.corundumstudio.socketio.annotation.OnDisconnect;
import com.corundumstudio.socketio.annotation.OnEvent;
import com.tripjoy.api.configuration.socketio.SocketRateLimiter;
import com.tripjoy.api.dto.response.ChatMessageResponse;
import com.tripjoy.api.dto.response.ConversationResponse;
import com.tripjoy.api.service.ISocketService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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

            server.getRoomOperations(roomName).getClients().stream()
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

            server.getRoomOperations(roomName).getClients().stream()
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
            log.error(
                    "Failed to broadcast message: conversationId={}, messageId={}",
                    conversationId,
                    messageResponse.getId(),
                    e);
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

    public void sendPinUpdate(UUID conversationId, UUID messageId, UUID userId, boolean isPinned) {
        try {
            String roomName = "conversation_" + conversationId;
            server.getRoomOperations(roomName).sendEvent("update_pin", messageId, userId, isPinned);
        } catch (Exception e) {
            log.error("Failed to broadcast pin update: {}", e.getMessage());
        }
    }

    /**
     * Send notification to a specific user
     * Broadcasts to user's personal room: "user_{userId}"
     */
    public void sendNotification(UUID userId, Object notification) {
        try {
            String roomName = "user_" + userId;
            server.getRoomOperations(roomName).sendEvent("notification", notification);
            log.info("Notification broadcasted to user: userId={}", userId);
        } catch (Exception e) {
            log.error("Failed to broadcast notification: userId={}, error={}", userId, e.getMessage());
        }
    }

    /**
     * Notify a user that a new Direct Conversation has been created for them.
     *
     * <p>Broadcasts {@code new_conversation} to the user's personal room {@code user_{userId}}.
     * The Socket.IO client should listen for this event and:
     * <ol>
     *   <li>Add the conversation to the inbox list</li>
     *   <li>Emit {@code join_conversation} with the new conversationId to start receiving messages</li>
     * </ol>
     */
    public void notifyNewDirectConversation(UUID userId, ConversationResponse conversation) {
        try {
            String roomName = "user_" + userId;
            server.getRoomOperations(roomName).sendEvent("new_conversation", conversation);
            log.info("Notified user {} of new direct conversation: {}", userId, conversation.getId());
        } catch (Exception e) {
            log.error("Failed to notify user {} of new DM conversation: {}", userId, e.getMessage());
        }
    }
}
