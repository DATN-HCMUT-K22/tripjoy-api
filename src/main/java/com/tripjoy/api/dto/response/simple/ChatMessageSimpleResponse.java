package com.tripjoy.api.dto.response.simple;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChatMessageSimpleResponse {

    @Schema(description = "ID of the replied message")
    UUID id;

    @JsonProperty("message_content")
    @Schema(description = "Short text content of the replied message")
    String messageContent;

    @JsonProperty("message_type")
    @Schema(description = "Type of the message (e.g., TEXT, IMAGE)")
    String messageType;

    // Lồng thông tin người gửi tin nhắn gốc
    @JsonProperty("sender")
    @Schema(description = "Information about the original message sender")
    UserSimpleResponse sender;
}