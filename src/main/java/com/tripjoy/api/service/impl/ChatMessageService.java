package com.tripjoy.api.service.impl;

import com.tripjoy.api.dto.event.MessageSentEvent;
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
import com.tripjoy.api.service.IChatMessageService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ChatMessageService implements IChatMessageService {

    UserRepository userRepository;
    ChatMessageRepository chatMessageRepository;
    ConversationRepository conversationRepository;
    ChatMessageMapper chatMessageMapper;
    ApplicationEventPublisher eventPublisher;

    @Transactional
    public void toggleLikeMessage(UUID messageId, UUID userId) {
        ChatMessage message = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new AppException(ErrorCode.MESSAGE_NOT_FOUND));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (message.getLikeUsers().contains(user)) {
            message.getLikeUsers().remove(user);
        } else {
            message.getLikeUsers().add(user);
        }

        chatMessageRepository.save(message);
    }

    @Transactional
    public ChatMessageResponse sendMessage(UUID conversationId, UUID senderId, ChatMessageRequest request) {
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new AppException(ErrorCode.CONVERSATION_NOT_FOUND));

        ChatMessage message = chatMessageMapper.toEntity(request);
        message.setConversation(conversation);
        message.setSender(sender);
        message.setCreatedAt(LocalDateTime.now());
        message.setIsDeleted(false);

        ChatMessage savedMessage = chatMessageRepository.save(message);

        conversation.setLastMessageTimestamp(LocalDateTime.now());
        conversationRepository.save(conversation);

        ChatMessageResponse response = chatMessageMapper.toResponse(savedMessage);

        MessageSentEvent event = MessageSentEvent.builder()
                .conversationId(conversationId)
                .messageResponse(response)
                .build();

        eventPublisher.publishEvent(event);

        return response;
    }
}
