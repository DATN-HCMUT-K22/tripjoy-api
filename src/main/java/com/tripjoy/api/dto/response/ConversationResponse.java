package com.tripjoy.api.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tripjoy.api.dto.response.simple.ChatMessageSimpleResponse;
import com.tripjoy.api.dto.response.simple.UserSimpleResponse;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.UUID;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ConversationResponse extends BaseResponse {

    UUID id;

    String type; // DIRECT / GROUP

    @JsonProperty("group_id")
    UUID groupId;

    String name; // Tên Group hoặc Tên người chat cùng

    String avatar; // Avatar Group hoặc Avatar người chat cùng

    @JsonProperty("last_message")
    ChatMessageSimpleResponse lastMessage; // Tin nhắn cuối cùng để hiện preview

    @JsonProperty("unread_count")
    Long unreadCount; // Số tin chưa đọc của user hiện tại

    @JsonProperty("is_pinned")
    Boolean isPinned;

    // Danh sách thành viên (Dùng SimpleResponse để tránh lặp)
    List<UserSimpleResponse> members;
}