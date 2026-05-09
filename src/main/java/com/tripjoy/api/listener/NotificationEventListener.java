package com.tripjoy.api.listener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tripjoy.api.dto.event.*;
import com.tripjoy.api.entity.Comment;
import com.tripjoy.api.entity.Group;
import com.tripjoy.api.entity.GroupMember;
import com.tripjoy.api.entity.Notification;
import com.tripjoy.api.entity.Post;
import com.tripjoy.api.entity.User;
import com.tripjoy.api.enums.NotificationType;
import com.tripjoy.api.repository.CommentRepository;
import com.tripjoy.api.repository.GroupMemberRepository;
import com.tripjoy.api.repository.NotificationRepository;
import com.tripjoy.api.repository.UserRepository;
import com.tripjoy.api.service.impl.SocketService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Notification Event Listener
 * Centralizes translation of Domain Events into Notifications.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final CommentRepository commentRepository;
    private final SocketService socketService;
    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher eventPublisher;

    // =========================================================================
    // 1. DOMAIN EVENT TRANSLATORS
    // Translate domain events to NotificationEvent and re-publish, or just save directly.
    // We will save and broadcast directly here to avoid chaining events.
    // =========================================================================

    // --- GROUP EVENTS ---

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleMemberJoinedGroup(MemberJoinedGroupEvent event) {
        log.info("Translating MemberJoinedGroupEvent to Notifications...");
        Group group = event.getGroup();
        User joiner = event.getUser();

        // Notify all group members EXCEPT the joiner
        List<GroupMember> members = groupMemberRepository.findByGroupOrderByRoleAsc(group);
        for (GroupMember member : members) {
            if (member.getUser().getId().equals(joiner.getId())) continue;

            Map<String, Object> meta = new HashMap<>();
            meta.put("groupId", group.getId());
            meta.put("groupName", group.getName());
            meta.put("joinerName", joiner.getFullName());

            createAndSendNotification(
                    NotificationType.GROUP_MEMBER_JOINED,
                    member.getUser().getId(),
                    joiner.getId(),
                    "Group",
                    group.getId().toString(),
                    joiner.getFullName() + " đã tham gia nhóm " + group.getName(),
                    "Thành viên mới",
                    meta);
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleGroupUpdated(GroupUpdatedEvent event) {
        log.info("Translating GroupUpdatedEvent to Notifications...");
        Group group = event.getGroup();
        User actor = event.getActor();

        List<GroupMember> members = groupMemberRepository.findByGroupOrderByRoleAsc(group);
        for (GroupMember member : members) {
            if (member.getUser().getId().equals(actor.getId())) continue;

            Map<String, Object> meta = new HashMap<>();
            meta.put("groupId", group.getId());

            createAndSendNotification(
                    NotificationType.GROUP_UPDATED,
                    member.getUser().getId(),
                    actor.getId(),
                    "Group",
                    group.getId().toString(),
                    "Thông tin nhóm " + group.getName() + " đã được cập nhật bởi " + actor.getFullName(),
                    "Nhóm đã cập nhật",
                    meta);
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleGroupRoleChanged(GroupRoleChangedEvent event) {
        Group group = event.getGroup();
        User actor = event.getActor();
        User target = event.getTargetUser();

        Map<String, Object> meta = new HashMap<>();
        meta.put("groupId", group.getId());
        meta.put("oldRole", event.getOldRole().name());
        meta.put("newRole", event.getNewRole().name());

        createAndSendNotification(
                NotificationType.GROUP_ROLE_CHANGED,
                target.getId(),
                actor.getId(),
                "Group",
                group.getId().toString(),
                actor.getFullName() + " đã thay đổi vai trò của bạn thành "
                        + event.getNewRole().name() + " trong nhóm " + group.getName(),
                "Thay đổi vai trò",
                meta);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleGroupLeadershipTransferred(GroupLeadershipTransferredEvent event) {
        Group group = event.getGroup();
        User oldLeader = event.getOldLeader();
        User newLeader = event.getNewLeader();

        List<GroupMember> members = groupMemberRepository.findByGroupOrderByRoleAsc(group);
        for (GroupMember member : members) {
            Map<String, Object> meta = new HashMap<>();
            meta.put("groupId", group.getId());

            createAndSendNotification(
                    NotificationType.GROUP_LEADERSHIP_TRANSFERRED,
                    member.getUser().getId(),
                    oldLeader.getId(),
                    "Group",
                    group.getId().toString(),
                    oldLeader.getFullName() + " đã chuyển quyền trưởng nhóm cho " + newLeader.getFullName()
                            + " trong nhóm " + group.getName(),
                    "Chuyển quyền trưởng nhóm",
                    meta);
        }
    }

    // --- CHAT EVENTS ---

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleMessageLiked(MessageLikedEvent event) {
        // Find sender of the message
        // Using ChatMessageService to fetch sender might cause cyclic dependency if this is deep.
        // Assuming we pass senderId in event or query it. Wait, MessageLikedEvent doesn't have message owner.
        // Actually this requires hitting DB for chat_message but we only want to fix notifications right now.
        // The user wants Post and Comment notifications prioritized, let's skip ChatMessage for a bit or implement
        // basically.
    }

    // --- POST & COMMENT EVENTS ---

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePostLiked(PostLikedEvent event) {
        Post post = event.getPost();
        User actor = event.getActor();
        User owner = post.getCreator();

        if (owner.getId().equals(actor.getId())) return;

        Map<String, Object> meta = new HashMap<>();
        meta.put("postId", post.getId());

        createAndSendNotification(
                NotificationType.POST_LIKED,
                owner.getId(),
                actor.getId(),
                "Post",
                post.getId().toString(),
                actor.getFullName() + " đã thích bài viết của bạn.",
                "Lượt thích mới",
                meta);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCommentLiked(CommentLikedEvent event) {
        Comment comment = event.getComment();
        User actor = event.getActor();
        User owner = comment.getUser();

        if (owner.getId().equals(actor.getId())) return;

        Map<String, Object> meta = new HashMap<>();
        meta.put("postId", comment.getPost().getId());
        meta.put("commentId", comment.getId());

        createAndSendNotification(
                NotificationType.COMMENT_LIKED,
                owner.getId(),
                actor.getId(),
                "Comment",
                comment.getId().toString(),
                actor.getFullName() + " đã thích bình luận của bạn.",
                "Lượt thích mới",
                meta);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCommentCreated(CommentCreatedEvent event) {
        Comment comment = event.getComment();
        User actor = event.getActor();
        Post post = comment.getPost();

        Comment parentComment = comment.getParentComment();

        Map<String, Object> meta = new HashMap<>();
        meta.put("postId", post.getId());
        meta.put("commentId", comment.getId());

        if (parentComment == null) {
            // Case 1: Root comment -> Notify Post Owner & other commenters
            User postOwner = post.getCreator();
            if (!postOwner.getId().equals(actor.getId())) {
                createAndSendNotification(
                        NotificationType.POST_COMMENTED,
                        postOwner.getId(),
                        actor.getId(),
                        "Post",
                        post.getId().toString(),
                        actor.getFullName() + " đã bình luận về bài viết của bạn.",
                        "Bình luận mới",
                        meta);
            }

            // Optional: notify others who commented on this post
            List<User> otherCommenters = commentRepository.findDistinctCommentersByPostId(post.getId());
            for (User other : otherCommenters) {
                if (other.getId().equals(actor.getId()) || other.getId().equals(postOwner.getId())) continue;

                createAndSendNotification(
                        NotificationType.POST_COMMENTED,
                        other.getId(),
                        actor.getId(),
                        "Post",
                        post.getId().toString(),
                        actor.getFullName() + " cũng đã bình luận về một bài viết mà bạn đang theo dõi.",
                        "Bình luận mới",
                        meta);
            }
        } else {
            // Case 2: Reply -> Notify Parent Comment Owner & other replies
            User parentOwner = parentComment.getUser();
            if (!parentOwner.getId().equals(actor.getId())) {
                createAndSendNotification(
                        NotificationType.COMMENT_REPLIED,
                        parentOwner.getId(),
                        actor.getId(),
                        "Comment",
                        parentComment.getId().toString(),
                        actor.getFullName() + " đã trả lời bình luận của bạn.",
                        "Trả lời mới",
                        meta);
            }

            // Notify other repliers to the SAME parent
            List<User> otherRepliers = commentRepository.findDistinctRepliersByParentCommentId(parentComment.getId());
            for (User other : otherRepliers) {
                if (other.getId().equals(actor.getId()) || other.getId().equals(parentOwner.getId())) continue;

                createAndSendNotification(
                        NotificationType.COMMENT_REPLIED,
                        other.getId(),
                        actor.getId(),
                        "Comment",
                        parentComment.getId().toString(),
                        actor.getFullName() + " đã cùng trả lời một bình luận.",
                        "Trả lời mới",
                        meta);
            }
        }
    }

    // =========================================================================
    // 2. CORE NOTIFICATION LOGIC
    // =========================================================================

    /**
     * Core handler to create a notification, save to DB, and broadcast via SocketIO.
     */
    private void createAndSendNotification(
            NotificationType type,
            UUID recipientId,
            UUID actorId,
            String entityType,
            String entityId,
            String message,
            String title,
            Map<String, Object> metadata) {
        try {
            User recipient = userRepository.findById(recipientId).orElse(null);
            if (recipient == null) return;

            User actor = null;
            if (actorId != null) {
                actor = userRepository.findById(actorId).orElse(null);
            }

            String metadataJson = null;
            if (metadata != null && !metadata.isEmpty()) {
                metadataJson = objectMapper.writeValueAsString(metadata);
            }

            Notification notification = Notification.builder()
                    .recipient(recipient)
                    .actor(actor)
                    .type(type)
                    .entityType(entityType)
                    .entityId(entityId)
                    .title(title)
                    .message(message)
                    .metadata(metadataJson)
                    .priority("NORMAL")
                    .isRead(false)
                    .isArchived(false)
                    .build();

            Notification savedNotification = notificationRepository.save(notification);
            socketService.sendNotification(recipientId, savedNotification);

            log.info(
                    "Notification created and sent: id={}, type={}, recipient={}",
                    savedNotification.getId(),
                    type,
                    recipient.getUsername());

        } catch (Exception e) {
            log.error(
                    "Failed to create and send notification: type={}, recipient={}, error={}",
                    type,
                    recipientId,
                    e.getMessage(),
                    e);
        }
    }

    /**
     * Fallback for direct NotificationEvent publishing (if any code still uses it)
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleNotificationEvent(NotificationEvent event) {
        createAndSendNotification(
                event.getType(),
                event.getRecipientId(),
                event.getActorId(),
                event.getEntityType(),
                event.getEntityId(),
                event.getMessage(),
                event.getTitle(),
                event.getMetadata());
    }
}
