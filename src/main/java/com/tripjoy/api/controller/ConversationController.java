package com.tripjoy.api.controller;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;

import com.tripjoy.api.constant.Endpoint;
import com.tripjoy.api.dto.request.chat.ChatMessageRequest;
import com.tripjoy.api.dto.request.chat.ConversationUpdateRequest;
import com.tripjoy.api.dto.request.chat.DirectConversationCreationRequest;
import com.tripjoy.api.dto.response.ApiResponse;
import com.tripjoy.api.dto.response.ChatMessageResponse;
import com.tripjoy.api.dto.response.ConversationResponse;
import com.tripjoy.api.dto.response.MessageCursorResponse;
import com.tripjoy.api.dto.response.MessageSearchResponse;
import com.tripjoy.api.service.IChatMessageService;
import com.tripjoy.api.service.IConversationService;
import com.tripjoy.api.utils.SecurityUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping(Endpoint.Conversation.BASE)
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Conversations (Chat)", description = "Manage conversations and messages")
public class ConversationController {

    IConversationService conversationService;
    IChatMessageService messageService;

    // --- QUẢN LÝ HỘI THOẠI ---

    @Operation(summary = "Get my conversations list (Inbox) - OK")
    @GetMapping
    public ApiResponse<List<ConversationResponse>> getMyConversations() {
        UUID currentUserId = SecurityUtils.getCurrentUserId();

        return ApiResponse.<List<ConversationResponse>>builder()
                .data(conversationService.getUserConversations(currentUserId))
                .build();
    }

    @Operation(
            summary = "Create or get existing 1-on-1 Direct conversation",
            description = """
                    Idempotent: if a DIRECT conversation between the two users already exists,
                    the existing conversation is returned — no duplicate is created.

                    On creation, both users receive a `new_conversation` Socket.IO event on their
                    personal room (`user_{userId}`). The client should:
                    1. Add the conversation to the inbox.
                    2. Emit `join_conversation` with the conversationId to start receiving messages.
                    """)
    @PostMapping
    public ApiResponse<ConversationResponse> createDirectConversation(
            @Valid @RequestBody DirectConversationCreationRequest request) {

        UUID currentUserId = SecurityUtils.getCurrentUserId();

        return ApiResponse.<ConversationResponse>builder()
                .data(conversationService.createDirectConversation(currentUserId, request.getTargetUserId()))
                .build();
    }

    @Operation(summary = "Get conversation details")
    @GetMapping(Endpoint.Conversation.ID)
    public ApiResponse<ConversationResponse> getConversationById(@PathVariable("conversationId") UUID conversationId) {

        UUID currentUserId = SecurityUtils.getCurrentUserId();

        return ApiResponse.<ConversationResponse>builder()
                .data(conversationService.getConversationDetail(conversationId, currentUserId))
                .build();
    }

    @Operation(summary = "Update conversation settings (name for group chats, isPinned)")
    @PutMapping(Endpoint.Conversation.ID)
    public ApiResponse<ConversationResponse> updateConversation(
            @PathVariable("conversationId") UUID conversationId, @Valid @RequestBody ConversationUpdateRequest request) {

        UUID currentUserId = SecurityUtils.getCurrentUserId();

        return ApiResponse.<ConversationResponse>builder()
                .data(conversationService.updateConversation(conversationId, request, currentUserId))
                .build();
    }

    @Operation(summary = "Reset unread count to 0 when user opens a chat")
    @PutMapping(Endpoint.Conversation.ID + "/read")
    public ApiResponse<Void> resetUnreadCount(@PathVariable("conversationId") UUID conversationId) {

        UUID currentUserId = SecurityUtils.getCurrentUserId();
        conversationService.resetUnreadCount(conversationId, currentUserId);

        return ApiResponse.<Void>builder()
                .message("Unread count reset to 0")
                .build();
    }

    // --- QUẢN LÝ TIN NHẮN (MESSAGES) ---

    @Operation(summary = "Send message to conversation - OK")
    @PostMapping(Endpoint.Conversation.MESSAGES)
    public ApiResponse<ChatMessageResponse> sendMessage(
            @PathVariable("conversationId") UUID conversationId, @Valid @RequestBody ChatMessageRequest request) {

        UUID senderId = SecurityUtils.getCurrentUserId();

        return ApiResponse.<ChatMessageResponse>builder()
                .data(messageService.sendMessage(conversationId, senderId, request))
                .build();
    }

    @Operation(
            summary = "Get message history with cursor-based pagination",
            description =
                    """
						Load messages in conversation using cursor-based pagination.
						- No params: Load latest 30 messages
						- ?before={timestamp}: Load older messages (scroll up)
						- ?after={timestamp}: Load newer messages
						- ?limit=50: Custom page size (max 100)
						""")
    @GetMapping(Endpoint.Conversation.MESSAGES)
    public ApiResponse<MessageCursorResponse> getMessages(
            @PathVariable("conversationId") UUID conversationId,
            @RequestParam(value = "before", required = false) String before,
            @RequestParam(value = "after", required = false) String after,
            @RequestParam(value = "limit", required = false) Integer limit) {

        UUID currentUserId = SecurityUtils.getCurrentUserId();

        return ApiResponse.<MessageCursorResponse>builder()
                .data(messageService.getMessages(conversationId, currentUserId, before, after, limit))
                .build();
    }

    @Operation(summary = "Get all pinned messages in conversation")
    @GetMapping(Endpoint.Conversation.PINNED_MESSAGES)
    public ApiResponse<List<ChatMessageResponse>> getPinnedMessages(@PathVariable("conversationId") UUID conversationId) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();

        return ApiResponse.<List<ChatMessageResponse>>builder()
                .data(messageService.getPinnedMessages(conversationId, currentUserId))
                .build();
    }

    @Operation(
            summary = "Search messages in conversation",
            description = "Full-text search messages using PostgreSQL FTS. "
                    + "Supports partial matching and relevance ranking.")
    @GetMapping(Endpoint.Conversation.SEARCH_MESSAGES)
    public ApiResponse<List<MessageSearchResponse>> searchMessages(
            @PathVariable("conversationId") UUID conversationId,
            @RequestParam("q") String q,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {

        UUID currentUserId = SecurityUtils.getCurrentUserId();
        return ApiResponse.<List<MessageSearchResponse>>builder()
                .data(messageService.searchMessages(conversationId, currentUserId, q, page, size))
                .build();
    }

    // --- SETTING CÁ NHÂN (MEMBERS) ---

    // @Operation(summary = "Leave conversation) -- Roi conversation dong nghia voi
    // roi group -> /api/v1/groups/{groupId}/members/me
    // @DeleteMapping(Endpoint.Conversation.MEMBERS)
    // public ApiResponse<Void> leaveConversation(@PathVariable UUID conversationId)
    // {
    //
    // UUID currentUserId = SecurityUtils.getCurrentUserId();
    //
    // // conversationService.leaveConversation(conversationId, currentUserId);
    //
    // return ApiResponse.<Void>builder().message("Left conversation").build();
    // }
}
