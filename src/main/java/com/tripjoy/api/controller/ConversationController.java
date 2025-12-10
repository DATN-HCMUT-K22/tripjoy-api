package com.tripjoy.api.controller;

import com.tripjoy.api.constant.Endpoint;
import com.tripjoy.api.dto.request.chat.ChatMessageRequest;
import com.tripjoy.api.dto.request.chat.DirectConversationCreationRequest;
import com.tripjoy.api.dto.response.ApiResponse;
import com.tripjoy.api.dto.response.ChatMessageResponse;
import com.tripjoy.api.dto.response.ConversationResponse;
import com.tripjoy.api.service.IConversationService;
import com.tripjoy.api.service.IChatMessageService;
import com.tripjoy.api.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(Endpoint.Conversation.BASE)
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Conversations (Chat)", description = "Manage conversations and messages")
public class ConversationController {

        IConversationService conversationService;
        IChatMessageService messageService; // Separate service for message handling if needed

        // --- QUẢN LÝ HỘI THOẠI ---

        @Operation(summary = "Get my conversations list (Inbox)")
        @GetMapping
        public ApiResponse<List<ConversationResponse>> getMyConversations() {
                // [FIX] Lấy current user ID từ Utils
                UUID currentUserId = SecurityUtils.getCurrentUserId();

                return ApiResponse.<List<ConversationResponse>>builder()
                                .data(conversationService.getUserConversations(currentUserId))
                                .build();
        }

        @Operation(summary = "Create 1-on-1 conversation (Direct Chat)")
        @PostMapping
        public ApiResponse<ConversationResponse> createDirectConversation(
                        @Valid @RequestBody DirectConversationCreationRequest request) {

                UUID currentUserId = SecurityUtils.getCurrentUserId();

                return ApiResponse.<ConversationResponse>builder()
                                // .data(conversationService.createDirectConversation(currentUserId,
                                // request.getTargetUserId()))
                                .build();
        }

        @Operation(summary = "Get conversation details")
        @GetMapping(Endpoint.Conversation.ID)
        public ApiResponse<ConversationResponse> getConversationById(@PathVariable UUID conversationId) {

                UUID currentUserId = SecurityUtils.getCurrentUserId();

                return ApiResponse.<ConversationResponse>builder()
                                // .data(conversationService.getConversationDetail(conversationId,
                                // currentUserId))
                                .build();
        }

        // --- QUẢN LÝ TIN NHẮN (MESSAGES) ---

        @Operation(summary = "Send message to conversation")
        @PostMapping(Endpoint.Conversation.MESSAGES)
        public ApiResponse<ChatMessageResponse> sendMessage(
                        @PathVariable UUID conversationId,
                        @Valid @RequestBody ChatMessageRequest request) {

                UUID senderId = SecurityUtils.getCurrentUserId();

                return ApiResponse.<ChatMessageResponse>builder()
                                .data(messageService.sendMessage(conversationId, senderId, request))
                                .build();
        }

        @Operation(summary = "Get message history (Paginated)")
        @GetMapping(Endpoint.Conversation.MESSAGES)
        public ApiResponse<Page<ChatMessageResponse>> getMessages(
                        @PathVariable UUID conversationId,
                        Pageable pageable) { // Spring will auto-inject page, size, sort from URL params

                UUID currentUserId = SecurityUtils.getCurrentUserId();

                return ApiResponse.<Page<ChatMessageResponse>>builder()
                                // .data(messageService.getMessages(conversationId, currentUserId, pageable))
                                .build();
        }

        // --- SETTING CÁ NHÂN (MEMBERS) ---

        @Operation(summary = "Leave conversation")
        @DeleteMapping(Endpoint.Conversation.MEMBERS)
        public ApiResponse<Void> leaveConversation(@PathVariable UUID conversationId) {

                UUID currentUserId = SecurityUtils.getCurrentUserId();

                // conversationService.leaveConversation(conversationId, currentUserId);

                return ApiResponse.<Void>builder().message("Left conversation").build();
        }
}