package com.tripjoy.api.dto.request.chat;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ConversationUpdateRequest {

    @Size(max = 100, message = "Name must not exceed 100 characters")
    @Schema(description = "Conversation name (only for group chats)", example = "Tokyo Trip 2024")
    String name;

    @JsonProperty("is_pinned")
    @Schema(description = "Pin/unpin the conversation in the list", example = "true")
    Boolean isPinned;
}
