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
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Chat", description = "Endpoints for loading chat history")
public class ChatController {

    ChatService chatService;

    // --- 1. Group chat history ---
    @Operation(summary = "Get group message history (paginated)")
    @GetMapping(Endpoint.API_PREFIX + Endpoint.Group.MESSAGES_BASE)
    public ApiResponse<Page<ChatMessageResponse>> getGroupMessages(
            @PathVariable String groupId, Pageable pageable) {

        // Service will return Page<ChatMessageResponse> including 'repliedToMessage'
        return ApiResponse.<Page<ChatMessageResponse>>builder()
//                .data(chatService.getGroupMessages(groupId, pageable))
                .build();
    }

    // --- 2. Direct (1-1) chat history ---
    @Operation(summary = "Get direct message history (1-1, paginated)")
    @GetMapping(Endpoint.Chat.BASE + Endpoint.Chat.DIRECT_MESSAGES)
    public ApiResponse<Page<ChatMessageResponse>> getDirectMessages(
            @PathVariable String userId, Pageable pageable) {

        // Service will return Page<ChatMessageResponse> including 'repliedToMessage'
        return ApiResponse.<Page<ChatMessageResponse>>builder()
//                .data(chatService.getDirectMessages(userId, pageable))
                .build();
    }

    // --- 3. (Fallback) Send message via REST ---
    @Operation(summary = "Send a message (fallback when WebSocket fails)")
    @PostMapping(Endpoint.API_PREFIX + Endpoint.Group.MESSAGES_BASE)
    public ApiResponse<ChatMessageResponse> sendGroupMessage(
            @PathVariable String groupId, @Valid @RequestBody ChatMessageRequest request) {

        // Service will return ChatMessageResponse including 'repliedToMessage'
        return ApiResponse.<ChatMessageResponse>builder()
//                .data(chatService.sendGroupMessage(groupId, request))
                .build();
    }
}
