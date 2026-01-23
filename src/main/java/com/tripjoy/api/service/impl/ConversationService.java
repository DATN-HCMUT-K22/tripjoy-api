package com.tripjoy.api.service.impl;

import com.tripjoy.api.dto.request.chat.ConversationUpdateRequest;
import com.tripjoy.api.dto.response.ConversationResponse;
import com.tripjoy.api.dto.response.simple.UserSimpleResponse;
import com.tripjoy.api.entity.Conversation;
import com.tripjoy.api.entity.ConversationMember;
import com.tripjoy.api.entity.User;
import com.tripjoy.api.enums.ConversationType;
import com.tripjoy.api.exception.AppException;
import com.tripjoy.api.exception.ErrorCode;
import com.tripjoy.api.mapper.ConversationMapper;
import com.tripjoy.api.repository.ConversationMemberRepository;
import com.tripjoy.api.repository.ConversationRepository;
import com.tripjoy.api.service.IConversationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConversationService implements IConversationService {

    private final ConversationRepository conversationRepository;
    private final ConversationMemberRepository conversationMemberRepository;
    private final ConversationMapper conversationMapper; // Inject Mapper

    public List<ConversationResponse> getUserConversations(UUID userId) {
        // Get conversations (only filter Group soft delete, not conversation)
        List<Conversation> conversations = conversationRepository.findAllByUserId(userId);

        return conversations.stream()
                .map(conv -> {
                    ConversationResponse response = conversationMapper.toResponse(conv, userId);
                    enrichConversationResponse(response, conv, userId);
                    return response;
                })
                .collect(Collectors.toList());
    }

    // Manually enrich response (because MapStruct @AfterMapping doesn't execute)
    private void enrichConversationResponse(ConversationResponse response, Conversation conversation,
            UUID currentUserId) {
        // 1. Set name and avatar based on type
        if (conversation.getType() == ConversationType.GROUP) {
            // For GROUP: use conversation name if set, otherwise group name
            if (conversation.getName() != null && !conversation.getName().trim().isEmpty()) {
                response.setName(conversation.getName());
            } else if (conversation.getGroup() != null) {
                response.setName(conversation.getGroup().getName());
            }

            // Set avatar from group
            if (conversation.getGroup() != null) {
                response.setAvatar(conversation.getGroup().getAvatar());
            }
        } else {
            // For DIRECT: get partner info
            User partner = conversation.getMembers().stream()
                    .map(ConversationMember::getUser)
                    .filter(user -> !user.getId().equals(currentUserId))
                    .findFirst()
                    .orElse(null);

            if (partner != null) {
                response.setName(partner.getFullName());
                response.setAvatar(partner.getAvatarUrl());
            } else {
                response.setName("Unknown User");
            }
        }

        // 2. Map members
        if (conversation.getMembers() != null && !conversation.getMembers().isEmpty()) {
            List<UserSimpleResponse> memberList = conversation.getMembers().stream()
                    .map(cm -> {
                        User user = cm.getUser();
                        if (user == null)
                            return null;
                        return UserSimpleResponse.builder()
                                .id(user.getId())
                                .username(user.getUsername())
                                .fullName(user.getFullName())
                                .avatarUrl(user.getAvatarUrl())
                                .build();
                    })
                    .filter(u -> u != null)
                    .collect(Collectors.toList());
            response.setMembers(memberList);
        }

        // 3. Set user-specific fields (unread count, pinned)
        ConversationMember myMemberInfo = conversation.getMembers().stream()
                .filter(m -> m.getUser() != null && m.getUser().getId().equals(currentUserId))
                .findFirst()
                .orElse(null);

        if (myMemberInfo != null) {
            response.setUnreadCount(myMemberInfo.getUnreadCount());
            response.setIsPinned(myMemberInfo.getIsPinned());
        }
    }

    public ConversationResponse getConversationDetail(UUID conversationId, UUID currentUserId) {
        Conversation conv = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new AppException(ErrorCode.CONVERSATION_NOT_FOUND));

        // Truyền currentUserId để mapper biết đường lấy tên/avatar đối phương
        ConversationResponse response = conversationMapper.toResponse(conv, currentUserId);
        enrichConversationResponse(response, conv, currentUserId);
        return response;
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
        ConversationResponse response = conversationMapper.toResponse(conversation, currentUserId);
        enrichConversationResponse(response, conversation, currentUserId);
        return response;
    }
}