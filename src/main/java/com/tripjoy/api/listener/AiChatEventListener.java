package com.tripjoy.api.listener;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.tripjoy.api.constant.AppConstants;
import com.tripjoy.api.dto.ai.AiChatRequestDto;
import com.tripjoy.api.dto.ai.AiChatResponseDto;
import com.tripjoy.api.dto.ai.AiFinalItineraryDto;
import com.tripjoy.api.dto.event.AiChatRequestedEvent;
import com.tripjoy.api.entity.Conversation;
import com.tripjoy.api.repository.ConversationRepository;
import com.tripjoy.api.service.IAiService;
import com.tripjoy.api.service.IChatMessageService;
import com.tripjoy.api.service.IItineraryGenerationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiChatEventListener {

    private final IAiService aiService;
    private final IChatMessageService chatMessageService;
    private final IItineraryGenerationService itineraryGenerationService;
    private final ConversationRepository conversationRepository;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleAiChatRequest(AiChatRequestedEvent event) {
        try {
            log.info("Handling AI Chat request for conversation: {}", event.getConversationId());

            Conversation conversation =
                    conversationRepository.findById(event.getConversationId()).orElse(null);

            AiFinalItineraryDto contextItinerary = null;
            if (conversation != null && conversation.getGroup() != null) {
                // Fetch the latest itinerary for the group to provide context to AI
                contextItinerary = itineraryGenerationService.getLatestAiItineraryByGroupId(
                        conversation.getGroup().getId());
            }

            AiChatRequestDto aiRequest = AiChatRequestDto.builder()
                    .conversationId(event.getConversationId().toString())
                    .message(event.getMessageContent())
                    .itinerary(contextItinerary)
                    .build();

            // Call AI Service synchronously inside this async thread
            AiChatResponseDto aiResponse = aiService.chat(aiRequest).block();

            if (aiResponse != null && aiResponse.getMessage() != null) {
                // Send the bot message and save it to DB. This will also fire a MessageSentEvent
                // which will be caught by MessageEventListener to broadcast via Socket.IO.
                chatMessageService.sendBotMessage(
                        event.getConversationId(), AppConstants.TRIPJOY_AI_USER_ID, aiResponse.getMessage());
                log.info("Successfully sent AI bot response to conversation: {}", event.getConversationId());
            } else {
                log.warn("AI Service returned empty response for conversation: {}", event.getConversationId());
            }

        } catch (Exception e) {
            log.error(
                    "Error occurred while processing AI Chat request for conversation: {}",
                    event.getConversationId(),
                    e);
            // Optionally, we could send a fallback error message from the bot to the chat
            try {
                chatMessageService.sendBotMessage(
                        event.getConversationId(),
                        AppConstants.TRIPJOY_AI_USER_ID,
                        "Xin lỗi, hiện tại tôi đang quá tải hoặc gặp sự cố. Vui lòng thử lại sau nhé!");
            } catch (Exception ex) {
                log.error("Failed to send fallback AI message", ex);
            }
        }
    }
}
