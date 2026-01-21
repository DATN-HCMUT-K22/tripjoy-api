package com.tripjoy.api.service.impl;

import com.tripjoy.api.dto.event.MessageSentEvent;
import com.tripjoy.api.dto.request.chat.ChatMessageRequest;
import com.tripjoy.api.dto.response.ChatMessageResponse;
import com.tripjoy.api.dto.response.MessageCursorResponse;
import com.tripjoy.api.entity.ChatMessage;
import com.tripjoy.api.entity.Conversation;
import com.tripjoy.api.entity.User;
import com.tripjoy.api.exception.AppException;
import com.tripjoy.api.exception.ErrorCode;
import com.tripjoy.api.mapper.ChatMessageMapper;
import com.tripjoy.api.repository.ChatMessageRepository;
import com.tripjoy.api.repository.ConversationRepository;
import com.tripjoy.api.repository.ConversationMemberRepository;
import com.tripjoy.api.repository.UserRepository;
import com.tripjoy.api.service.IChatMessageService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ChatMessageService implements IChatMessageService {

    UserRepository userRepository;
    ChatMessageRepository chatMessageRepository;
    ConversationRepository conversationRepository;
    ConversationMemberRepository conversationMemberRepository;
    ChatMessageMapper chatMessageMapper;
    ApplicationEventPublisher eventPublisher;

    @Transactional
    public void likeMessage(UUID messageId, UUID userId) {
        ChatMessage message = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new AppException(ErrorCode.MESSAGE_NOT_FOUND));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // Check if already liked
        if (message.getLikeUsers().contains(user)) {
            throw new AppException(ErrorCode.MESSAGE_ALREADY_LIKED);
        }

        message.getLikeUsers().add(user);
        chatMessageRepository.save(message);
    }

    @Transactional
    public void unlikeMessage(UUID messageId, UUID userId) {
        ChatMessage message = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new AppException(ErrorCode.MESSAGE_NOT_FOUND));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // Check if not liked
        if (!message.getLikeUsers().contains(user)) {
            throw new AppException(ErrorCode.MESSAGE_NOT_LIKED);
        }

        message.getLikeUsers().remove(user);
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
        // SoftDeleteInfo is already initialized by default

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

    @Transactional
    public void pinMessage(UUID conversationId, UUID messageId, UUID userId) {
        // 1. Verify message exists
        ChatMessage message = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new AppException(ErrorCode.MESSAGE_NOT_FOUND));

        // 2. Verify message belongs to conversation
        if (!message.getConversation().getId().equals(conversationId)) {
            throw new AppException(ErrorCode.MESSAGE_NOT_IN_CONVERSATION);
        }

        // 3. Verify user is member of conversation
        boolean isMember = conversationMemberRepository.existsByConversationIdAndUserId(conversationId, userId);
        if (!isMember) {
            throw new AppException(ErrorCode.USER_NOT_IN_CONVERSATION);
        }

        // 4. Check if already pinned
        if (message.getIsPinned()) {
            throw new AppException(ErrorCode.MESSAGE_ALREADY_PINNED);
        }

        // 5. Check pin limit (50 max)
        long pinnedCount = chatMessageRepository.countPinnedByConversationId(conversationId);
        if (pinnedCount >= 50) {
            throw new AppException(ErrorCode.PIN_LIMIT_EXCEEDED);
        }

        // 6. Pin the message
        message.setIsPinned(true);
        chatMessageRepository.save(message);
    }

    @Transactional
    public void unpinMessage(UUID conversationId, UUID messageId, UUID userId) {
        // 1. Verify message exists
        ChatMessage message = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new AppException(ErrorCode.MESSAGE_NOT_FOUND));

        // 2. Verify user is member of conversation
        boolean isMember = conversationMemberRepository.existsByConversationIdAndUserId(conversationId, userId);
        if (!isMember) {
            throw new AppException(ErrorCode.USER_NOT_IN_CONVERSATION);
        }

        // 3. Check if not pinned
        if (!message.getIsPinned()) {
            throw new AppException(ErrorCode.MESSAGE_NOT_PINNED);
        }

        // 4. Unpin the message
        message.setIsPinned(false);
        chatMessageRepository.save(message);
    }

    @Transactional(readOnly = true)
    public List<ChatMessageResponse> getPinnedMessages(UUID conversationId, UUID userId) {
        // 1. Verify user is member of conversation
        boolean isMember = conversationMemberRepository.existsByConversationIdAndUserId(conversationId, userId);
        if (!isMember) {
            throw new AppException(ErrorCode.USER_NOT_IN_CONVERSATION);
        }

        // 2. Fetch all pinned messages (ordered by createdAt DESC)
        List<ChatMessage> pinnedMessages = chatMessageRepository.findPinnedByConversationId(conversationId);

        // 3. Map to response DTOs
        return pinnedMessages.stream()
                .map(chatMessageMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public MessageCursorResponse getMessages(
            UUID conversationId,
            UUID currentUserId,
            String before,
            String after,
            Integer limit) {

        // 1. Verify user is member
        boolean isMember = conversationMemberRepository
                .existsByConversationIdAndUserId(conversationId, currentUserId);
        if (!isMember) {
            throw new AppException(ErrorCode.USER_NOT_IN_CONVERSATION);
        }

        // 2. Set default limit (30 messages)
        int pageSize = (limit != null && limit > 0 && limit <= 100) ? limit : 30;
        Pageable pageable = PageRequest.of(0, pageSize + 1); // +1 to check hasMore

        List<ChatMessage> messages;

        // 3. Determine query type
        if (before != null) {
            // Load older messages (scroll up)
            LocalDateTime beforeTime = LocalDateTime.parse(before);
            messages = chatMessageRepository.findMessagesBefore(
                    conversationId, beforeTime, pageable);
        } else if (after != null) {
            // Load newer messages
            LocalDateTime afterTime = LocalDateTime.parse(after);
            messages = chatMessageRepository.findMessagesAfter(
                    conversationId, afterTime, pageable);
            Collections.reverse(messages); // Reverse to DESC order
        } else {
            // Initial load: latest messages
            messages = chatMessageRepository.findLatestMessages(
                    conversationId, pageable);
        }

        // 4. Check if has more
        boolean hasMore = messages.size() > pageSize;
        if (hasMore) {
            messages = messages.subList(0, pageSize); // Remove extra item
        }

        // 5. Map to DTOs
        List<ChatMessageResponse> messageResponses = messages.stream()
                .map(chatMessageMapper::toResponse)
                .collect(Collectors.toList());

        // 6. Build cursors
        MessageCursorResponse.CursorInfo cursors = null;
        if (!messageResponses.isEmpty()) {
            String firstTimestamp = messageResponses.get(0).getCreatedAt().toString();
            String lastTimestamp = messageResponses.get(messageResponses.size() - 1)
                    .getCreatedAt().toString();

            cursors = MessageCursorResponse.CursorInfo.builder()
                    .before(firstTimestamp) // Use latest message timestamp
                    .after(lastTimestamp) // Use oldest message timestamp
                    .build();
        }

        // 7. Build hasMore info
        MessageCursorResponse.HasMore hasMoreInfo = MessageCursorResponse.HasMore.builder()
                .before(hasMore) // Has older messages
                .after(false) // Client should use WebSocket for new messages
                .build();

        return MessageCursorResponse.builder()
                .messages(messageResponses)
                .cursors(cursors)
                .hasMore(hasMoreInfo)
                .build();
    }
}
