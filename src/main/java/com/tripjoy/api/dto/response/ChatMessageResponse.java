package com.tripjoy.api.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tripjoy.api.dto.response.simple.ChatMessageSimpleResponse;
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
public class ChatMessageResponse {

    UUID id; // id từ "Chat_message"

    @JsonProperty("message_type")
    String messageType;

    @JsonProperty("message_content")
    String messageContent;

    @JsonProperty("media_url")
    String mediaUrl;

    @JsonProperty("share_post_url")
    String sharePostUrl;

    @JsonProperty("is_bot")
    Boolean isBot;

    String status;

    @JsonProperty("created_at")
    LocalDateTime createdAt;

    @JsonProperty("sender_id")
    String senderId;

    @JsonProperty("receiver_id")
    String receiverId;

    @JsonProperty("group_id")
    String groupId;

    @JsonProperty("reply_message_id")
    @Schema(description = "ID of the message being replied to (if any)")
    String replyMessageId; // Vẫn giữ ID gốc

    @JsonProperty("replied_to_message")
    @Schema(description = "Summary information of the message being replied to")
    ChatMessageSimpleResponse repliedToMessage; // Object lồng nhau
}