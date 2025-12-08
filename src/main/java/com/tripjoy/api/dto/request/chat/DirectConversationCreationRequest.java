package com.tripjoy.api.dto.request.chat;


import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DirectConversationCreationRequest {
    @NotNull(message = "Target User ID is required")
    UUID targetUserId;
}
