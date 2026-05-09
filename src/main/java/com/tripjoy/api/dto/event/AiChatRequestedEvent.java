package com.tripjoy.api.dto.event;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiChatRequestedEvent {
    private UUID conversationId;
    private UUID senderId;
    private String messageContent;
}
