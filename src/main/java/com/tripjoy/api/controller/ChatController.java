package com.tripjoy.api.controller;

import com.tripjoy.api.constant.Endpoint;
import com.tripjoy.api.dto.request.ChatMessageRequest;
import com.tripjoy.api.dto.response.ApiResponse;
import com.tripjoy.api.dto.response.ChatMessageResponse;
import com.tripjoy.api.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(Endpoint.Chat.BASE) // -> /api/v1/chat
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Chat", description = "Endpoints for ALL chat functionalities (Groups & Direct)")
public class ChatController {

    ChatService chatService;

    // --- 1. Lấy lịch sử tin nhắn Groups ---
    @Operation(summary = "Get group message history (paginated)")
    @GetMapping(Endpoint.Chat.GROUP_MESSAGES) // -> /groups/{groupId}/messages
    public ApiResponse<Page<ChatMessageResponse>> getGroupMessages(
            @PathVariable UUID groupId, Pageable pageable) {

        return ApiResponse.<Page<ChatMessageResponse>>builder()
//                .data(chatService.getGroupMessages(groupId, pageable))
                .build();
    }

    // --- 2. Gửi tin nhắn Groups (Fallback) ---
    @Operation(summary = "Send a group message (fallback)")
    @PostMapping(Endpoint.Chat.GROUP_MESSAGES) // -> /groups/{groupId}/messages
    public ApiResponse<ChatMessageResponse> sendGroupMessage(
            @PathVariable UUID groupId, @Valid @RequestBody ChatMessageRequest request) {

        return ApiResponse.<ChatMessageResponse>builder()
//                .data(chatService.sendGroupMessage(groupId, request))
                .build();
    }

    // --- 3. Lấy lịch sử tin nhắn 1-1 (Direct) ---
    @Operation(summary = "Get direct message history (1-1, paginated)")
    @GetMapping(Endpoint.Chat.DIRECT_MESSAGES) // -> /direct/{userId}/messages
    public ApiResponse<Page<ChatMessageResponse>> getDirectMessages(
            @PathVariable UUID userId, Pageable pageable) {

        return ApiResponse.<Page<ChatMessageResponse>>builder()
//                .data(chatService.getDirectMessages(userId, pageable))
                .build();
    }

    // --- 4. Gửi tin nhắn 1-1 (Fallback) ---
    @Operation(summary = "Send a direct message (fallback)")
    @PostMapping(Endpoint.Chat.DIRECT_MESSAGES) // -> /direct/{userId}/messages
    public ApiResponse<ChatMessageResponse> sendDirectMessage(
            @PathVariable UUID userId, @Valid @RequestBody ChatMessageRequest request) {

        return ApiResponse.<ChatMessageResponse>builder()
//                .data(chatService.sendDirectMessage(userId, request))
                .build();
    }

    // --- 5. Tương tác (Like/Unlike) tin nhắn ---
    // (Áp dụng cho cả tin nhắn group và direct)

    @Operation(summary = "Like a chat message (Create a 'like' resource)")
    @PostMapping(Endpoint.Chat.MESSAGE_LIKES) // -> /messages/{messageId}/likes
    public ApiResponse<Void> likeChatMessage(@PathVariable UUID messageId) {

        // chatService.likeMessage(messageId);
        return ApiResponse.<Void>builder().message("Message liked").build();
    }

    @Operation(summary = "Unlike a chat message (Delete a 'like' resource)")
    @DeleteMapping(Endpoint.Chat.MESSAGE_LIKES) // -> /messages/{messageId}/likes
    public ApiResponse<Void> unlikeChatMessage(@PathVariable UUID messageId) {

        // chatService.unlikeMessage(messageId);
        return ApiResponse.<Void>builder().message("Message unliked").build();
    }
}
