package com.tripjoy.api.listener;

import com.tripjoy.api.dto.event.MessageSentEvent;
import com.tripjoy.api.service.impl.SocketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

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
}
