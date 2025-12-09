package com.tripjoy.api.service.impl;

import com.tripjoy.api.dto.response.ConversationResponse;
import com.tripjoy.api.entity.Conversation;
import com.tripjoy.api.exception.AppException;
import com.tripjoy.api.exception.ErrorCode;
import com.tripjoy.api.mapper.ConversationMapper;
import com.tripjoy.api.repository.ConversationRepository;
import com.tripjoy.api.service.IConversationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ConversationService implements IConversationService {

    private final ConversationRepository conversationRepository;
    private final ConversationMapper conversationMapper; // Inject Mapper

    public List<ConversationResponse> getUserConversations(UUID userId) {
        // 1. Lấy danh sách conversation từ DB
        List<Conversation> conversations = conversationRepository.findAllByUserId(userId);

        // 2. Map sang Response, truyền userId vào làm Context
        return conversations.stream()
                .map(conv -> conversationMapper.toResponse(conv, userId)) // Truyền userId vào đây
                .toList();
    }

    public ConversationResponse getConversationDetail(UUID conversationId, UUID currentUserId) {
        Conversation conv = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new AppException(ErrorCode.CONVERSATION_NOT_FOUND));

        // Truyền currentUserId để mapper biết đường lấy tên/avatar đối phương
        return conversationMapper.toResponse(conv, currentUserId);
    }
}