package com.tripjoy.api.dto.event;

import com.tripjoy.api.dto.response.ChatMessageResponse;
import lombok.*;

import java.util.UUID;

/**
 * Event fired after a chat message is successfully saved to database.
 * Used to trigger real-time Socket.IO broadcast AFTER transaction commits.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageSentEvent {
    private UUID conversationId;
    private ChatMessageResponse messageResponse;
}
