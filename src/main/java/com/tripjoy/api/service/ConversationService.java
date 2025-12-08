package com.tripjoy.api.service;

import com.tripjoy.api.dto.response.ConversationResponse;
import com.tripjoy.api.entity.Conversation;
import com.tripjoy.api.mapper.ConversationMapper;
import com.tripjoy.api.repository.ConversationRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ConversationService {

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
                .orElseThrow(() -> new RuntimeException("Not found"));

        // Truyền currentUserId để mapper biết đường lấy tên/avatar đối phương
        return conversationMapper.toResponse(conv, currentUserId);
    }
}