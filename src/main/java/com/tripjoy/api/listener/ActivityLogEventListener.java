package com.tripjoy.api.listener;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.tripjoy.api.dto.event.*;
import com.tripjoy.api.entity.ChatMessage;
import com.tripjoy.api.entity.User;
import com.tripjoy.api.enums.ActivityAction;
import com.tripjoy.api.repository.ChatMessageRepository;
import com.tripjoy.api.repository.UserRepository;
import com.tripjoy.api.service.IActivityLogService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Translates domain events into {@code ActivityLog} records.
 *
 * <h2>Design principles</h2>
 * <ul>
 *   <li><b>Zero coupling</b> — services publish domain events with no knowledge of
 *       this listener. Activity logging is a pure cross-cutting concern.</li>
 *   <li><b>Non-blocking</b> — every handler is {@code @Async}, so the calling
 *       thread is released immediately after publishing the event.</li>
 *   <li><b>Transactional safety</b> — handlers for events raised inside a
 *       transaction use {@code AFTER_COMMIT}, guaranteeing we only log what was
 *       actually committed. Auth events (no transaction) use a plain
 *       {@code @EventListener}.</li>
 *   <li><b>Failsafe</b> — every handler is wrapped in a try-catch. A logging
 *       failure must never propagate back to the business layer.</li>
 * </ul>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ActivityLogEventListener {

    private final IActivityLogService activityLogService;
    private final UserRepository userRepository;
    private final ChatMessageRepository chatMessageRepository;

    // =========================================================================
    // AUTH EVENTS — No surrounding transaction; use plain @EventListener
    // =========================================================================

    @Async
    @EventListener
    public void onUserRegistered(UserRegisteredEvent event) {
        try {
            activityLogService.log(
                    event.getUser(),
                    ActivityAction.USER_REGISTERED,
                    "USER",
                    event.getUser().getId().toString(),
                    null,
                    null);
        } catch (Exception ex) {
            log.error("ActivityLog: failed to log USER_REGISTERED for userId={}: {}",
                    event.getUser().getId(), ex.getMessage(), ex);
        }
    }

    @Async
    @EventListener
    public void onUserLoggedIn(UserLoggedInEvent event) {
        try {
            activityLogService.log(
                    event.getUser(),
                    ActivityAction.USER_LOGIN,
                    "USER",
                    event.getUser().getId().toString(),
                    null,
                    null);
        } catch (Exception ex) {
            log.error("ActivityLog: failed to log USER_LOGIN for userId={}: {}",
                    event.getUser().getId(), ex.getMessage(), ex);
        }
    }

    @Async
    @EventListener
    public void onUserLoggedOut(UserLoggedOutEvent event) {
        try {
            User user = userRepository.findById(event.getUserId()).orElse(null);
            if (user == null) {
                log.warn("ActivityLog: USER_LOGOUT — user not found for id={}", event.getUserId());
                return;
            }
            activityLogService.log(
                    user,
                    ActivityAction.USER_LOGOUT,
                    "USER",
                    event.getUserId().toString(),
                    null,
                    null);
        } catch (Exception ex) {
            log.error("ActivityLog: failed to log USER_LOGOUT for userId={}: {}",
                    event.getUserId(), ex.getMessage(), ex);
        }
    }

    // =========================================================================
    // POST EVENTS — @TransactionalEventListener(AFTER_COMMIT)
    // =========================================================================

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPostCreated(PostCreatedEvent event) {
        try {
            Map<String, Object> meta = new HashMap<>();
            meta.put("postId", event.getPost().getId());

            activityLogService.log(
                    event.getActor(),
                    ActivityAction.POST_CREATED,
                    "POST",
                    event.getPost().getId().toString(),
                    meta,
                    null);
        } catch (Exception ex) {
            log.error("ActivityLog: failed to log POST_CREATED: {}", ex.getMessage(), ex);
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPostLiked(PostLikedEvent event) {
        try {
            Map<String, Object> meta = new HashMap<>();
            meta.put("postId", event.getPost().getId());
            meta.put("postOwnerId", event.getPost().getCreator().getId());

            activityLogService.log(
                    event.getActor(),
                    ActivityAction.POST_LIKED,
                    "POST",
                    event.getPost().getId().toString(),
                    meta,
                    null);
        } catch (Exception ex) {
            log.error("ActivityLog: failed to log POST_LIKED: {}", ex.getMessage(), ex);
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPostUnliked(PostUnlikedEvent event) {
        try {
            Map<String, Object> meta = new HashMap<>();
            meta.put("postId", event.getPost().getId());

            activityLogService.log(
                    event.getActor(),
                    ActivityAction.POST_UNLIKED,
                    "POST",
                    event.getPost().getId().toString(),
                    meta,
                    null);
        } catch (Exception ex) {
            log.error("ActivityLog: failed to log POST_UNLIKED: {}", ex.getMessage(), ex);
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPostSaved(PostSavedEvent event) {
        try {
            Map<String, Object> meta = new HashMap<>();
            meta.put("postId", event.getPost().getId());

            activityLogService.log(
                    event.getActor(),
                    ActivityAction.POST_SAVED,
                    "POST",
                    event.getPost().getId().toString(),
                    meta,
                    null);
        } catch (Exception ex) {
            log.error("ActivityLog: failed to log POST_SAVED: {}", ex.getMessage(), ex);
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPostUnsaved(PostUnsavedEvent event) {
        try {
            Map<String, Object> meta = new HashMap<>();
            meta.put("postId", event.getPost().getId());

            activityLogService.log(
                    event.getActor(),
                    ActivityAction.POST_UNSAVED,
                    "POST",
                    event.getPost().getId().toString(),
                    meta,
                    null);
        } catch (Exception ex) {
            log.error("ActivityLog: failed to log POST_UNSAVED: {}", ex.getMessage(), ex);
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPostDeleted(PostDeletedEvent event) {
        try {
            Map<String, Object> meta = new HashMap<>();
            meta.put("postId", event.getPostId());

            activityLogService.log(
                    event.getActor(),
                    ActivityAction.POST_DELETED,
                    "POST",
                    event.getPostId().toString(),
                    meta,
                    null);
        } catch (Exception ex) {
            log.error("ActivityLog: failed to log POST_DELETED: {}", ex.getMessage(), ex);
        }
    }

    // =========================================================================
    // COMMENT EVENTS
    // =========================================================================

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onCommentCreated(CommentCreatedEvent event) {
        try {
            Map<String, Object> meta = new HashMap<>();
            meta.put("commentId", event.getComment().getId());
            meta.put("postId", event.getComment().getPost().getId());
            boolean isReply = event.getComment().getParentComment() != null;
            meta.put("isReply", isReply);
            if (isReply) {
                meta.put("parentCommentId", event.getComment().getParentComment().getId());
            }

            activityLogService.log(
                    event.getActor(),
                    ActivityAction.COMMENT_CREATED,
                    "COMMENT",
                    event.getComment().getId().toString(),
                    meta,
                    null);
        } catch (Exception ex) {
            log.error("ActivityLog: failed to log COMMENT_CREATED: {}", ex.getMessage(), ex);
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onCommentLiked(CommentLikedEvent event) {
        try {
            Map<String, Object> meta = new HashMap<>();
            meta.put("commentId", event.getComment().getId());
            meta.put("postId", event.getComment().getPost().getId());

            activityLogService.log(
                    event.getActor(),
                    ActivityAction.COMMENT_LIKED,
                    "COMMENT",
                    event.getComment().getId().toString(),
                    meta,
                    null);
        } catch (Exception ex) {
            log.error("ActivityLog: failed to log COMMENT_LIKED: {}", ex.getMessage(), ex);
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onCommentDeleted(CommentDeletedEvent event) {
        try {
            Map<String, Object> meta = new HashMap<>();
            meta.put("commentId", event.getCommentId());

            activityLogService.log(
                    event.getActor(),
                    ActivityAction.COMMENT_DELETED,
                    "COMMENT",
                    event.getCommentId().toString(),
                    meta,
                    null);
        } catch (Exception ex) {
            log.error("ActivityLog: failed to log COMMENT_DELETED: {}", ex.getMessage(), ex);
        }
    }

    // =========================================================================
    // GROUP EVENTS
    // =========================================================================

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onGroupCreated(GroupCreatedEvent event) {
        try {
            Map<String, Object> meta = new HashMap<>();
            meta.put("groupId", event.getGroup().getId());
            meta.put("groupName", event.getGroup().getName());
            meta.put("initialMemberCount", event.getInitialMembers() != null
                    ? event.getInitialMembers().size() : 0);

            activityLogService.log(
                    event.getCreator(),
                    ActivityAction.GROUP_CREATED,
                    "GROUP",
                    event.getGroup().getId().toString(),
                    meta,
                    null);
        } catch (Exception ex) {
            log.error("ActivityLog: failed to log GROUP_CREATED: {}", ex.getMessage(), ex);
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onGroupUpdated(GroupUpdatedEvent event) {
        try {
            Map<String, Object> meta = new HashMap<>();
            meta.put("groupId", event.getGroup().getId());
            meta.put("groupName", event.getGroup().getName());

            activityLogService.log(
                    event.getActor(),
                    ActivityAction.GROUP_UPDATED,
                    "GROUP",
                    event.getGroup().getId().toString(),
                    meta,
                    null);
        } catch (Exception ex) {
            log.error("ActivityLog: failed to log GROUP_UPDATED: {}", ex.getMessage(), ex);
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onMemberJoinedGroup(MemberJoinedGroupEvent event) {
        try {
            Map<String, Object> meta = new HashMap<>();
            meta.put("groupId", event.getGroup().getId());
            meta.put("groupName", event.getGroup().getName());

            activityLogService.log(
                    event.getUser(),
                    ActivityAction.GROUP_JOINED,
                    "GROUP",
                    event.getGroup().getId().toString(),
                    meta,
                    null);
        } catch (Exception ex) {
            log.error("ActivityLog: failed to log GROUP_JOINED: {}", ex.getMessage(), ex);
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onMemberRemovedFromGroup(MemberRemovedFromGroupEvent event) {
        try {
            Map<String, Object> meta = new HashMap<>();
            meta.put("groupId", event.getGroup().getId());
            meta.put("groupName", event.getGroup().getName());
            meta.put("removedUserId", event.getRemovedUser().getId());
            meta.put("removedUsername", event.getRemovedUser().getUsername());

            // Actor is the one who initiated the remove (could be the removed user themselves for "leave")
            activityLogService.log(
                    event.getRemovedByUser(),
                    ActivityAction.GROUP_MEMBER_REMOVED,
                    "GROUP",
                    event.getGroup().getId().toString(),
                    meta,
                    null);
        } catch (Exception ex) {
            log.error("ActivityLog: failed to log GROUP_MEMBER_REMOVED: {}", ex.getMessage(), ex);
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onGroupRoleChanged(GroupRoleChangedEvent event) {
        try {
            Map<String, Object> meta = new HashMap<>();
            meta.put("groupId", event.getGroup().getId());
            meta.put("targetUserId", event.getTargetUser().getId());
            meta.put("oldRole", event.getOldRole().name());
            meta.put("newRole", event.getNewRole().name());

            activityLogService.log(
                    event.getActor(),
                    ActivityAction.GROUP_ROLE_CHANGED,
                    "GROUP",
                    event.getGroup().getId().toString(),
                    meta,
                    null);
        } catch (Exception ex) {
            log.error("ActivityLog: failed to log GROUP_ROLE_CHANGED: {}", ex.getMessage(), ex);
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onGroupLeadershipTransferred(GroupLeadershipTransferredEvent event) {
        try {
            Map<String, Object> meta = new HashMap<>();
            meta.put("groupId", event.getGroup().getId());
            meta.put("newLeaderId", event.getNewLeader().getId());
            meta.put("newLeaderUsername", event.getNewLeader().getUsername());

            activityLogService.log(
                    event.getOldLeader(),
                    ActivityAction.GROUP_ROLE_CHANGED,
                    "GROUP",
                    event.getGroup().getId().toString(),
                    meta,
                    null);
        } catch (Exception ex) {
            log.error("ActivityLog: failed to log GROUP_LEADERSHIP_TRANSFERRED: {}", ex.getMessage(), ex);
        }
    }

    // =========================================================================
    // ITINERARY EVENTS
    // =========================================================================

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onItineraryCreated(ItineraryCreatedEvent event) {
        try {
            Map<String, Object> meta = new HashMap<>();
            meta.put("itineraryId", event.getItinerary().getId());
            meta.put("name", event.getItinerary().getName());
            if (event.getItinerary().getGroup() != null) {
                meta.put("groupId", event.getItinerary().getGroup().getId());
            }

            activityLogService.log(
                    event.getActor(),
                    ActivityAction.ITINERARY_CREATED,
                    "ITINERARY",
                    event.getItinerary().getId().toString(),
                    meta,
                    null);
        } catch (Exception ex) {
            log.error("ActivityLog: failed to log ITINERARY_CREATED: {}", ex.getMessage(), ex);
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onItineraryDeleted(ItineraryDeletedEvent event) {
        try {
            Map<String, Object> meta = new HashMap<>();
            meta.put("itineraryId", event.getItineraryId());

            activityLogService.log(
                    event.getActor(),
                    ActivityAction.ITINERARY_DELETED,
                    "ITINERARY",
                    event.getItineraryId().toString(),
                    meta,
                    null);
        } catch (Exception ex) {
            log.error("ActivityLog: failed to log ITINERARY_DELETED: {}", ex.getMessage(), ex);
        }
    }

    // =========================================================================
    // CHAT / MESSAGE EVENTS
    // =========================================================================

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onMessageSent(MessageSentEvent event) {
        try {
            if (event.getMessageResponse() == null) return;

            String senderIdStr = event.getMessageResponse().getSenderId();
            if (senderIdStr == null) return;

            UUID senderId = UUID.fromString(senderIdStr);
            User sender = userRepository.findById(senderId).orElse(null);
            if (sender == null) return;

            Map<String, Object> meta = new HashMap<>();
            meta.put("conversationId", event.getConversationId());
            meta.put("messageId", event.getMessageResponse().getId());
            meta.put("messageType", event.getMessageResponse().getMessageType());

            activityLogService.log(
                    sender,
                    ActivityAction.MESSAGE_SENT,
                    "CONVERSATION",
                    event.getConversationId().toString(),
                    meta,
                    null);
        } catch (Exception ex) {
            log.error("ActivityLog: failed to log MESSAGE_SENT: {}", ex.getMessage(), ex);
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onMessageLiked(MessageLikedEvent event) {
        try {
            if (event.getUserId() == null) return;

            User user = userRepository.findById(event.getUserId()).orElse(null);
            if (user == null) return;

            Map<String, Object> meta = new HashMap<>();
            meta.put("conversationId", event.getConversationId());
            meta.put("messageId", event.getMessageId());

            activityLogService.log(
                    user,
                    ActivityAction.MESSAGE_LIKED,
                    "MESSAGE",
                    event.getMessageId().toString(),
                    meta,
                    null);
        } catch (Exception ex) {
            log.error("ActivityLog: failed to log MESSAGE_LIKED: {}", ex.getMessage(), ex);
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onMessageRecalled(MessageRecalledEvent event) {
        try {
            ChatMessage message = chatMessageRepository.findById(event.getMessageId()).orElse(null);
            if (message == null) return;

            Map<String, Object> meta = new HashMap<>();
            meta.put("conversationId", event.getConversationId());
            meta.put("messageId", event.getMessageId());

            activityLogService.log(
                    message.getSender(),
                    ActivityAction.MESSAGE_DELETED,
                    "MESSAGE",
                    event.getMessageId().toString(),
                    meta,
                    null);
        } catch (Exception ex) {
            log.error("ActivityLog: failed to log MESSAGE_DELETED (recall): {}", ex.getMessage(), ex);
        }
    }
}
