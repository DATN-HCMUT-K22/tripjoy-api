package com.tripjoy.api.controller;

import com.tripjoy.api.constant.Endpoint;
import com.tripjoy.api.dto.request.chat.ChatMessageRequest;
import com.tripjoy.api.dto.request.chat.DirectConversationCreationRequest;
import com.tripjoy.api.dto.response.ApiResponse;
import com.tripjoy.api.dto.response.ChatMessageResponse;
import com.tripjoy.api.dto.response.ConversationResponse;
import com.tripjoy.api.entity.User;
import com.tripjoy.api.service.ConversationService;
import com.tripjoy.api.service.MessageService;
import com.tripjoy.api.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(Endpoint.Conversation.BASE)
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Conversations (Chat)", description = "Quản lý hội thoại và tin nhắn")
public class ConversationController {

    ConversationService conversationService;
    MessageService messageService; // Tách service xử lý message riêng nếu cần

    // --- QUẢN LÝ HỘI THOẠI ---


    @Operation(summary = "Lấy danh sách hội thoại của tôi (Inbox)")
    @GetMapping
    public ApiResponse<List<ConversationResponse>> getMyConversations() {
        // [FIX] Lấy current user ID từ Utils
        UUID currentUserId = SecurityUtils.getCurrentUserId();

        return ApiResponse.<List<ConversationResponse>>builder()
                .data(conversationService.getUserConversations(currentUserId))
                .build();
    }

    @Operation(summary = "Tạo cuộc trò chuyện 1-1 (Direct Chat)")
    @PostMapping
    public ApiResponse<ConversationResponse> createDirectConversation(
            @Valid @RequestBody DirectConversationCreationRequest request) {

        UUID currentUserId = SecurityUtils.getCurrentUserId();

        return ApiResponse.<ConversationResponse>builder()
//                .data(conversationService.createDirectConversation(currentUserId, request.getTargetUserId()))
                .build();
    }

    @Operation(summary = "Lấy chi tiết hội thoại")
    @GetMapping(Endpoint.Conversation.ID)
    public ApiResponse<ConversationResponse> getConversationById(@PathVariable UUID conversationId) {

        UUID currentUserId = SecurityUtils.getCurrentUserId();

        return ApiResponse.<ConversationResponse>builder()
//                .data(conversationService.getConversationDetail(conversationId, currentUserId))
                .build();
    }

    // --- QUẢN LÝ TIN NHẮN (MESSAGES) ---

    @Operation(summary = "Gửi tin nhắn vào hội thoại")
    @PostMapping(Endpoint.Conversation.MESSAGES)
    public ApiResponse<ChatMessageResponse> sendMessage(
            @PathVariable UUID conversationId,
            @Valid @RequestBody ChatMessageRequest request) {

        UUID senderId = SecurityUtils.getCurrentUserId();

        return ApiResponse.<ChatMessageResponse>builder()
                .data(messageService.sendMessage(conversationId, senderId, request))
                .build();
    }

    @Operation(summary = "Lấy lịch sử tin nhắn (Phân trang)")
    @GetMapping(Endpoint.Conversation.MESSAGES)
    public ApiResponse<Page<ChatMessageResponse>> getMessages(
            @PathVariable UUID conversationId,
            Pageable pageable) { // Spring sẽ tự inject page, size, sort từ URL param

        UUID currentUserId = SecurityUtils.getCurrentUserId();

        return ApiResponse.<Page<ChatMessageResponse>>builder()
//                .data(messageService.getMessages(conversationId, currentUserId, pageable))
                .build();
    }

    // --- SETTING CÁ NHÂN (MEMBERS) ---

    @Operation(summary = "Rời khỏi cuộc trò chuyện")
    @DeleteMapping(Endpoint.Conversation.MEMBERS)
    public ApiResponse<Void> leaveConversation(@PathVariable UUID conversationId) {

        UUID currentUserId = SecurityUtils.getCurrentUserId();

//        conversationService.leaveConversation(conversationId, currentUserId);

        return ApiResponse.<Void>builder().message("Left conversation").build();
    }
}