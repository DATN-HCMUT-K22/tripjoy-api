package com.tripjoy.api.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ConversationCreationRequest {

    // Nếu tạo Group Chat thì cần name, Direct thì null
    String name;

    // Danh sách user ID muốn add vào cuộc trò chuyện
    @NotNull
    @Size(min = 1)
    @JsonProperty("member_ids")
    Set<UUID> memberIds;
}