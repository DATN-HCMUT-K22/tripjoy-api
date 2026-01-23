package com.tripjoy.api.service.impl;

import com.tripjoy.api.dto.request.chat.ConversationUpdateRequest;
import com.tripjoy.api.dto.response.ConversationResponse;
import com.tripjoy.api.entity.Conversation;
import com.tripjoy.api.entity.ConversationMember;
import com.tripjoy.api.enums.ConversationType;
import com.tripjoy.api.exception.AppException;
import com.tripjoy.api.exception.ErrorCode;
import com.tripjoy.api.mapper.ConversationMapper;
import com.tripjoy.api.repository.ConversationMemberRepository;
import com.tripjoy.api.repository.ConversationRepository;
import com.tripjoy.api.service.IConversationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ConversationService implements IConversationService {

    private final ConversationRepository conversationRepository;
    private final ConversationMemberRepository conversationMemberRepository;
    private final ConversationMapper conversationMapper; // Inject Mapper

    public List<ConversationResponse> getUserConversations(UUID userId) {
        // Get conversations (only filter Group soft delete, not conversation)
        List<Conversation> conversations = conversationRepository.findAllByUserId(userId);

        // Map to Response, pass userId as context
        return conversations.stream()
                .map(conv -> conversationMapper.toResponse(conv, userId))
                .toList();
    }

    public ConversationResponse getConversationDetail(UUID conversationId, UUID currentUserId) {
        Conversation conv = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new AppException(ErrorCode.CONVERSATION_NOT_FOUND));

        // Truyền currentUserId để mapper biết đường lấy tên/avatar đối phương
        return conversationMapper.toResponse(conv, currentUserId);
    }

    @Transactional
    public ConversationResponse updateConversation(UUID conversationId, ConversationUpdateRequest request,
            UUID currentUserId) {
        // 1. Find conversation
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new AppException(ErrorCode.CONVERSATION_NOT_FOUND));

        // 2. Verify user is member
        ConversationMember member = conversationMemberRepository
                .findByConversationIdAndUserId(conversationId, currentUserId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_IN_CONVERSATION));

        // 3. Update conversation name (only for GROUP conversations)
        if (request.getName() != null) {
            if (conversation.getType() == ConversationType.DIRECT) {
                throw new AppException(ErrorCode.CANNOT_UPDATE_DIRECT_CHAT_NAME);
            }
            conversation.setName(request.getName());
            conversationRepository.save(conversation);
        }

        // 4. Update pinned status (member-specific)
        if (request.getIsPinned() != null) {
            member.setIsPinned(request.getIsPinned());
            conversationMemberRepository.save(member);
        }

        // 5. Return updated conversation
        return conversationMapper.toResponse(conversation, currentUserId);
    }
}