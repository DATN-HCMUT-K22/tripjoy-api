package com.tripjoy.api.listener;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.tripjoy.api.dto.event.MessageRecalledEvent;
import com.tripjoy.api.dto.event.MessageSentEvent;
import com.tripjoy.api.service.impl.SocketService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageEventListener {

    private final SocketService socketService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleMessageSent(MessageSentEvent event) {
        try {
            socketService.sendNewMessage(event.getConversationId(), event.getMessageResponse());
        } catch (Exception e) {
            log.error("Failed to broadcast message via Socket.IO: {}", e.getMessage());
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleMessageLiked(com.tripjoy.api.dto.event.MessageLikedEvent event) {
        try {
            socketService.sendLikeUpdate(event.getConversationId(), event.getMessageId(), event.getUserId(), true);
        } catch (Exception e) {
            log.error("Failed to broadcast message like via Socket.IO: {}", e.getMessage());
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleMessageUnliked(com.tripjoy.api.dto.event.MessageUnlikedEvent event) {
        try {
            socketService.sendLikeUpdate(event.getConversationId(), event.getMessageId(), event.getUserId(), false);
        } catch (Exception e) {
            log.error("Failed to broadcast message unlike via Socket.IO: {}", e.getMessage());
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleMessagePinned(com.tripjoy.api.dto.event.MessagePinnedEvent event) {
        try {
            socketService.sendPinUpdate(event.getConversationId(), event.getMessageId(), event.getUserId(), true);
        } catch (Exception e) {
            log.error("Failed to broadcast message pin via Socket.IO: {}", e.getMessage());
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleMessageUnpinned(com.tripjoy.api.dto.event.MessageUnpinnedEvent event) {
        try {
            socketService.sendPinUpdate(event.getConversationId(), event.getMessageId(), event.getUserId(), false);
        } catch (Exception e) {
            log.error("Failed to broadcast message unpin via Socket.IO: {}", e.getMessage());
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleMessageRecalled(MessageRecalledEvent event) {
        try {
            socketService.sendRecallUpdate(event.getConversationId(), event.getMessageId());
        } catch (Exception e) {
            log.error("Failed to broadcast message recall via Socket.IO: {}", e.getMessage());
        }
    }
}
