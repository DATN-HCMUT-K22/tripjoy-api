package com.tripjoy.api.dto.request.chat;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DirectConversationCreationRequest {
    @NotNull(message = "Target User ID is required")
    UUID targetUserId;
}
