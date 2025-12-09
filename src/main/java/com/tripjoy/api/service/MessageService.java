package com.tripjoy.api.service;

import com.tripjoy.api.dto.request.chat.ChatMessageRequest;
import com.tripjoy.api.dto.response.ChatMessageResponse;
import com.tripjoy.api.entity.ChatMessage;
import com.tripjoy.api.entity.Conversation;
import com.tripjoy.api.entity.User;
import com.tripjoy.api.exception.AppException;
import com.tripjoy.api.exception.ErrorCode;
import com.tripjoy.api.mapper.ChatMessageMapper;
import com.tripjoy.api.repository.ChatMessageRepository;
import com.tripjoy.api.repository.ConversationRepository;
import com.tripjoy.api.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MessageService {
    private final ChatMessageRepository messageRepository;
    private final UserRepository userRepository;
    private final SocketService socketService;
    private final ChatMessageRepository chatMessageRepository;
    private final ConversationRepository conversationRepository;
    private final ChatMessageMapper chatMessageMapper;

    @Transactional
    public void toggleLikeMessage(UUID messageId, UUID userId) {
        // 1. Tìm tin nhắn
        ChatMessage message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));

        // 2. Tìm user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 3. Logic Toggle (Có rồi thì xóa, chưa có thì thêm)
        // Lưu ý: message.getLikeUsers() trả về Set<User>
        if (message.getLikeUsers().contains(user)) {
            message.getLikeUsers().remove(user); // Unlike
        } else {
            message.getLikeUsers().add(user); // Like
        }

        // 4. Lưu lại
        messageRepository.save(message);

        // 5. [REAL-TIME] Quan trọng: Gửi sự kiện qua Socket để client cập nhật icon tim ngay lập tức
        // socketService.sendLikeUpdate(message.getConversation().getId(), messageId, userId, isLiked);
    }

    @Transactional
    public ChatMessageResponse sendMessage(UUID conversationId, UUID senderId, ChatMessageRequest request) {
        // 0. Tìm Sender
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        // 1. Validate Conversation
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new AppException(ErrorCode.CONVERSATION_NOT_FOUND));

        // 2. Map Request -> Entity
        ChatMessage message = chatMessageMapper.toEntity(request);
        message.setConversation(conversation);
        message.setSender(sender);
        message.setCreatedAt(LocalDateTime.now());
        message.setIsDeleted(false);
        // message.setStatus("SENT");

        // 3. Save DB (Logic quan trọng nằm ở đây)
        ChatMessage savedMessage = chatMessageRepository.save(message);

        // 4. Update Conversation (Last Message & Timestamp)
        conversation.setLastMessageTimestamp(LocalDateTime.now());
        // conversation.setLastMessage(savedMessage); // Nếu có field này
        conversationRepository.save(conversation);

        // 5. Map Entity -> Response
        ChatMessageResponse response = chatMessageMapper.toResponse(savedMessage);

        // 6. [SOCKET] Bắn tin nhắn realtime
        // Gọi SocketService để báo cho mọi người biết
        socketService.sendNewMessage(conversationId, response);

        return response;
    }
}
