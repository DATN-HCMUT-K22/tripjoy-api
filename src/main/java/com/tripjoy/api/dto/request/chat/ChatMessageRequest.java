package com.tripjoy.api.dto.request.chat;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChatMessageRequest {

    @NotBlank
    @JsonProperty("message_content")
    @Schema(description = "Text content of the message", example = "Hello!")
    String messageContent;

    @Builder.Default
    @JsonProperty("message_type")
    @Schema(description = "Type of the message", example = "TEXT")
    String messageType = "TEXT"; // "TEXT", "IMAGE", "SHARE_POST"

    @JsonProperty("media_url")
    @Schema(description = "Media URL when message_type is IMAGE", example = "http://image.url/img.png")
    String mediaUrl;

    @JsonProperty("share_post_url")
    @Schema(description = "Post URL when message_type is SHARE_POST", example = "http://tripjoy/posts/abc")
    String sharePostUrl;

    @JsonProperty("parent_message_id")
    @Schema(description = "ID of the message being replied to (if any)")
    String parentMessageId;
}