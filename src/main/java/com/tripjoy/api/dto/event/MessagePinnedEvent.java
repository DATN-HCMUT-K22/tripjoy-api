package com.tripjoy.api.dto.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessagePinnedEvent {
    private UUID conversationId;
    private UUID messageId;
    private UUID userId;
    private LocalDateTime pinnedAt;
}
