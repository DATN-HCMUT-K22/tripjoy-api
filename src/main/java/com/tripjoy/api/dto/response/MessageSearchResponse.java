package com.tripjoy.api.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tripjoy.api.dto.response.simple.UserSimpleResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(description = "Search result for a message")
public class MessageSearchResponse {

    UUID id;

    @JsonProperty("conversation_id")
    UUID conversationId;

    @JsonProperty("sender_id")
    UUID senderId;

    @JsonProperty("sender")
    UserSimpleResponse sender;

    @JsonProperty("message_content")
    String messageContent;

    @JsonProperty("message_type")
    String messageType;

    @JsonProperty("media_url")
    String mediaUrl;

    @JsonProperty("is_pinned")
    Boolean isPinned;

    @JsonProperty("created_at")
    LocalDateTime createdAt;
}
