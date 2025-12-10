package com.tripjoy.api.listener;

import com.tripjoy.api.dto.event.GroupCreatedEvent;
import com.tripjoy.api.dto.event.MemberJoinedGroupEvent;
import com.tripjoy.api.entity.Conversation;
import com.tripjoy.api.entity.ConversationMember;
import com.tripjoy.api.entity.User;
import com.tripjoy.api.enums.ConversationType;
import com.tripjoy.api.repository.ConversationMemberRepository;
import com.tripjoy.api.repository.ConversationRepository;
import com.tripjoy.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class GroupEventListener {

    private final ConversationRepository conversationRepository;
    private final ConversationMemberRepository conversationMemberRepository;
    private final UserRepository userRepository;

    /**
     * CASE 1: Khi tạo Group mới -> Tự động tạo đoạn chat "General"
     * Logic này giúp GroupService không cần dependency vào ConversationRepository
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleGroupCreated(GroupCreatedEvent event) {
        try {
            log.info("EVENT: Group Created {} -> Init default conversation", event.getGroup().getId());

            // 1. Tạo Conversation "General Chat"
            Conversation defaultConv = Conversation.builder()
                    .group(event.getGroup())
                    .type(ConversationType.GROUP) // Đảm bảo bạn có Enum này
                    .name("General Chat")
                    .build();
            conversationRepository.save(defaultConv);

            // 2. Add người tạo (Owner) vào Conversation đó luôn
            ConversationMember chatMember = ConversationMember.builder()
                    .conversation(defaultConv)
                    .user(event.getCreator())
                    .isPinned(false)
                    .isMuted(false)
                    .unreadCount(0L)
                    .build();
            conversationMemberRepository.save(chatMember);

            log.info("-> Created 'General Chat' for Group {}", event.getGroup().getName());
        } catch (Exception e) {
            log.error("ERROR handling GroupCreatedEvent: ", e);
        }
    }

    /**
     * CASE 2: Khi thêm thành viên vào Group -> Sync vào các đoạn chat của Group đó
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleMemberJoinedGroup(MemberJoinedGroupEvent event) {
        try {


            // 1. Lấy thông tin User và Group từ Event
            User user = event.getUser();
            UUID groupId = event.getGroup().getId();

            log.info("EVENT: SYNC CHAT -> User {} joined Group {}", user.getId(), groupId);

            // 2. Lấy tất cả Conversation của Group đó
            List<Conversation> conversations = conversationRepository.findByGroup_Id(groupId);

            if (conversations.isEmpty()) {
                log.warn("Group {} has no conversations to sync", groupId);
                return;
            }

            // 3. Add User vào từng Conversation
            int count = 0;
            for (Conversation conversation : conversations) {
                // Kiểm tra xem đã tồn tại chưa để tránh lỗi Duplicate Key
                boolean exists = conversationMemberRepository.existsByConversationIdAndUserId(conversation.getId(), user.getId());

                if (!exists) {
                    ConversationMember newMember = ConversationMember.builder()
                            .conversation(conversation)
                            .user(user)
                            .isMuted(false)
                            .isPinned(false)
                            .unreadCount(0L)
                            .build();

                    conversationMemberRepository.save(newMember);
                    count++;
                }
            }
            log.info("-> Successfully added User {} to {} conversations", user.getId(), count);
        } catch (Exception e) {
            log.error("CRITICAL ERROR in handleMemberJoinedGroup: ", e);
        }
    }
}