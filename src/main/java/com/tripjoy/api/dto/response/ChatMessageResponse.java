package com.tripjoy.api.dto.response;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tripjoy.api.dto.response.simple.ChatMessageSimpleResponse;
import com.tripjoy.api.dto.response.simple.UserSimpleResponse;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChatMessageResponse extends BaseResponse {

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

    // createdAt đã có từ BaseResponse - REMOVED

    @JsonProperty("sender_id")
    String senderId;

    @JsonProperty("sender")
    UserSimpleResponse sender;

    @JsonProperty("conversation_id")
    UUID conversationId;

    @JsonProperty("parent_message_id")
    @Schema(description = "ID of the message being replied to (if any)")
    String parentMessageId;

    @JsonProperty("parent_message")
    @Schema(description = "Summary information of the message being replied to")
    ChatMessageSimpleResponse parentMessage;

    @JsonProperty("is_pinned")
    @Schema(description = "Whether this message is pinned in the conversation")
    Boolean isPinned;

    @JsonProperty("like_count")
    @Schema(description = "Total number of likes on this message")
    Integer likeCount;

    @JsonProperty("is_liked_by_current_user")
    @Schema(description = "Whether the current user has liked this message")
    Boolean isLikedByCurrentUser;
}
