package com.tripjoy.api.service.impl;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.annotation.OnConnect;
import com.corundumstudio.socketio.annotation.OnDisconnect;
import com.corundumstudio.socketio.annotation.OnEvent;
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

    // --- 1. CONNECTION MANAGEMENT ---

    @OnConnect
    public void onConnect(SocketIOClient client) {
        String userId = client.getHandshakeData().getSingleUrlParam("userId");
        if (userId != null) {
            // Join room riêng của User để nhận thông báo cá nhân (Notification)
            client.joinRoom("user_" + userId);
            log.info("Socket: User connected: {}", userId);
        }
    }

    @OnDisconnect
    public void onDisconnect(SocketIOClient client) {
        log.info("Socket: Client disconnected: {}", client.getSessionId());
    }

    // --- 2. ROOM MANAGEMENT (Client chủ động join vào room chat) ---

    // Client khi mở màn hình chat nào thì emit event này để join room đó
    @OnEvent("join_conversation")
    public void onJoinConversation(SocketIOClient client, String conversationId) {
        // Room name: "conversation_uuid"
        String roomName = "conversation_" + conversationId;
        client.joinRoom(roomName);
        log.info("Socket: Client joined room: {}", roomName);
    }

    // Client khi thoát màn hình chat thì leave room (để đỡ nhận tin rác)
    @OnEvent("leave_conversation")
    public void onLeaveConversation(SocketIOClient client, String conversationId) {
        String roomName = "conversation_" + conversationId;
        client.leaveRoom(roomName);
        log.info("Socket: Client left room: {}", roomName);
    }

    // --- 3. TYPING INDICATOR (Hiệu ứng đang gõ...) ---

    @OnEvent("typing")
    public void onTyping(SocketIOClient client, String conversationId) {
        // Báo cho những người khác trong phòng là "Có ai đó đang gõ"
        // Exclude client gửi (mình gõ thì mình không cần nhận tin mình đang gõ)
        client.getNamespace()
                .getRoomOperations("conversation_" + conversationId)
                .sendEvent("user_typing", client.getHandshakeData().getSingleUrlParam("userId"));
    }

    @OnEvent("stop_typing")
    public void onStopTyping(SocketIOClient client, String conversationId) {
        client.getNamespace()
                .getRoomOperations("conversation_" + conversationId)
                .sendEvent("user_stop_typing");
    }

    // --- 4. BROADCAST METHODS (Được gọi từ MessageService) ---

    /**
     * Hàm này được MessageService gọi SAU KHI lưu tin nhắn vào DB thành công.
     * Nhiệm vụ: Gửi tin nhắn đến tất cả user đang online trong conversation đó.
     */
    public void sendNewMessage(UUID conversationId, ChatMessageResponse messageResponse) {
        String roomName = "conversation_" + conversationId.toString();

        // Gửi event "receive_message" kèm cục data response chuẩn
        server.getRoomOperations(roomName)
                .sendEvent("receive_message", messageResponse);

        log.info("Socket: Broadcast message {} to room {}", messageResponse.getId(), roomName);
    }

    // Ví dụ gửi thông báo like
    public void sendLikeUpdate(UUID conversationId, UUID messageId, UUID userId, boolean isLiked) {
        server.getRoomOperations("conversation_" + conversationId.toString())
                .sendEvent("update_like", messageId, userId, isLiked);
    }
}