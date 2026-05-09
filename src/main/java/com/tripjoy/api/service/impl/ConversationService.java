package com.tripjoy.api.service.impl;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tripjoy.api.dto.request.chat.ConversationUpdateRequest;
import com.tripjoy.api.dto.response.ConversationResponse;
import com.tripjoy.api.dto.response.simple.ChatMessageSimpleResponse;
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
import com.tripjoy.api.repository.UserRepository;
import com.tripjoy.api.service.IConversationService;
import com.tripjoy.api.service.ISocketService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConversationService implements IConversationService {

    private final ConversationRepository conversationRepository;
    private final ConversationMemberRepository conversationMemberRepository;
    private final ConversationMapper conversationMapper;
    private final UserRepository userRepository;
    private final ISocketService socketService;

    public List<ConversationResponse> getUserConversations(UUID userId) {
        List<Conversation> conversations = conversationRepository.findAllByUserId(userId);

        return conversations.stream()
                .map(conv -> {
                    ConversationResponse response = conversationMapper.toResponse(conv, userId);
                    enrichConversationResponse(response, conv, userId);
                    return response;
                })
                .collect(Collectors.toList());
    }

    /**
     * Create or retrieve an existing Direct Message (1-on-1) conversation.
     *
     * <h3>Flow:</h3>
     * <ol>
     *   <li>Validate: không tự nhắn mình</li>
     *   <li>Validate: target user tồn tại</li>
     *   <li>Idempotency: nếu DM đã tồn tại → trả về conversation cũ</li>
     *   <li>Tạo {@code Conversation(type=DIRECT)}</li>
     *   <li>Tạo 2 {@code ConversationMember} records (initiator + target)</li>
     *   <li>Broadcast {@code new_conversation} event qua Socket.IO tới cả 2 users</li>
     *   <li>Return {@code ConversationResponse}</li>
     * </ol>
     */
    @Override
    @Transactional
    public ConversationResponse createDirectConversation(UUID initiatorId, UUID targetUserId) {
        // 1. Validate: không tự nhắn mình
        if (initiatorId.equals(targetUserId)) {
            throw new AppException(ErrorCode.CANNOT_SELF_DIRECT_MESSAGE);
        }

        // 2. Validate: target user phải tồn tại
        User initiator =
                userRepository.findById(initiatorId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        User target =
                userRepository.findById(targetUserId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // 3. Idempotency: nếu DM đã tồn tại → trả về conversation cũ
        //    Không tạo duplicate dù client gọi API nhiều lần
        Optional<Conversation> existing = conversationRepository.findDirectConversation(initiatorId, targetUserId);

        if (existing.isPresent()) {
            log.info(
                    "DM already exists between {} and {} — returning existing conversation: {}",
                    initiatorId,
                    targetUserId,
                    existing.get().getId());
            Conversation existingConv = existing.get();
            ConversationResponse response = conversationMapper.toResponse(existingConv, initiatorId);
            enrichConversationResponse(response, existingConv, initiatorId);
            return response;
        }

        // 4. Tạo Conversation mới (type = DIRECT, không có group)
        Conversation conversation =
                Conversation.builder().type(ConversationType.DIRECT).build();
        conversation = conversationRepository.save(conversation);
        log.info(
                "Created new DIRECT conversation: {} between {} and {}",
                conversation.getId(),
                initiatorId,
                targetUserId);

        // 5. Tạo ConversationMember cho cả 2 user
        ConversationMember initiatorMember = ConversationMember.builder()
                .conversation(conversation)
                .user(initiator)
                .isPinned(false)
                .isMuted(false)
                .unreadCount(0L)
                .build();

        ConversationMember targetMember = ConversationMember.builder()
                .conversation(conversation)
                .user(target)
                .isPinned(false)
                .isMuted(false)
                .unreadCount(0L)
                .build();

        conversationMemberRepository.save(initiatorMember);
        conversationMemberRepository.save(targetMember);

        // Manually set members before enrichment because L1 cache won't fetch them immediately
        conversation.setMembers(Set.of(initiatorMember, targetMember));

        // 6. Build response
        ConversationResponse response = conversationMapper.toResponse(conversation, initiatorId);
        enrichConversationResponse(response, conversation, initiatorId);

        // 7. Thông báo qua Socket.IO tới cả 2 users
        //    → Client nhận "new_conversation" trên room "user_{id}" và tự join conversation room
        ConversationResponse responseForTarget = conversationMapper.toResponse(conversation, targetUserId);
        enrichConversationResponse(responseForTarget, conversation, targetUserId);

        socketService.notifyNewDirectConversation(initiatorId, response);
        socketService.notifyNewDirectConversation(targetUserId, responseForTarget);

        return response;
    }

    // Manually enrich response (because MapStruct @AfterMapping doesn't execute)
    private void enrichConversationResponse(
            ConversationResponse response, Conversation conversation, UUID currentUserId) {
        // 1. Set name and avatar based on type
        if (conversation.getType() == ConversationType.GROUP) {
            // For GROUP: use conversation name if set, otherwise group name
            if (conversation.getName() != null && !conversation.getName().trim().isEmpty()) {
                response.setName(conversation.getName());
            } else if (conversation.getGroup() != null) {
                response.setName("General chat"); // Default fallback
            }

            // Set avatar and groupName from group
            if (conversation.getGroup() != null) {
                response.setAvatar(conversation.getGroup().getAvatar());
                response.setGroupName(conversation.getGroup().getName());
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
                        if (user == null) return null;
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

        // 4. Map Last Message (from cached denormalized fields)
        if (conversation.getLastMessageId() != null) {
            UserSimpleResponse sender = null;
            if (conversation.getLastMessageSenderId() != null) {
                sender = com.tripjoy.api.dto.response.simple.UserSimpleResponse.builder()
                        .id(conversation.getLastMessageSenderId())
                        .fullName(conversation.getLastMessageSenderName())
                        .avatarUrl(conversation.getLastMessageSenderAvatar())
                        .build();
            }

            ChatMessageSimpleResponse lastMsg = ChatMessageSimpleResponse.builder()
                    .id(conversation.getLastMessageId())
                    .messageContent(conversation.getLastMessageContent())
                    .messageType(conversation.getLastMessageType())
                    .sender(sender)
                    .build();

            response.setLastMessage(lastMsg);
        }
    }

    public ConversationResponse getConversationDetail(UUID conversationId, UUID currentUserId) {
        Conversation conv = conversationRepository
                .findById(conversationId)
                .orElseThrow(() -> new AppException(ErrorCode.CONVERSATION_NOT_FOUND));

        // Truyền currentUserId để mapper biết đường lấy tên/avatar đối phương
        ConversationResponse response = conversationMapper.toResponse(conv, currentUserId);
        enrichConversationResponse(response, conv, currentUserId);
        return response;
    }

    @Transactional
    public ConversationResponse updateConversation(
            UUID conversationId, ConversationUpdateRequest request, UUID currentUserId) {
        // 1. Find conversation
        Conversation conversation = conversationRepository
                .findById(conversationId)
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

    @Transactional
    public void resetUnreadCount(UUID conversationId, UUID currentUserId) {
        // Verify user is member
        boolean isMember = conversationMemberRepository.existsByConversationIdAndUserId(conversationId, currentUserId);
        if (!isMember) {
            throw new AppException(ErrorCode.USER_NOT_IN_CONVERSATION);
        }

        conversationMemberRepository.resetUnreadCount(conversationId, currentUserId);
    }
}
