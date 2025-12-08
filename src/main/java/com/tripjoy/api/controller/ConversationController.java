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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.awt.print.Pageable;
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
    public ApiResponse<List<ConversationResponse>> getMyConversations(
            @AuthenticationPrincipal User currentUser) {
        return ApiResponse.<List<ConversationResponse>>builder()
//                .data(conversationService.getUserConversations(currentUser.getId()))
                .build();
    }

    @Operation(summary = "Tạo cuộc trò chuyện 1-1 (Direct Chat)")
    @PostMapping
    public ApiResponse<ConversationResponse> createDirectConversation(
            @Valid @RequestBody DirectConversationCreationRequest request,
            @AuthenticationPrincipal User currentUser) {
        // Lưu ý: Group Chat không tạo ở đây, nó được tạo tự động qua Event từ GroupController
        return ApiResponse.<ConversationResponse>builder()
//                .data(conversationService.createDirectConversation(currentUser, request.getTargetUserId()))
                .build();
    }

    @Operation(summary = "Lấy chi tiết hội thoại")
    @GetMapping(Endpoint.Conversation.ID)
    public ApiResponse<ConversationResponse> getConversationById(
            @PathVariable UUID conversationId,
            @AuthenticationPrincipal User currentUser) {
        // Cần check quyền xem user có trong conversation này không
        return ApiResponse.<ConversationResponse>builder()
//                .data(conversationService.getConversationDetail(conversationId, currentUser.getId()))
                .build();
    }

    // --- QUẢN LÝ TIN NHẮN (MESSAGES) ---

    @Operation(summary = "Gửi tin nhắn vào hội thoại")
    @PostMapping(Endpoint.Conversation.MESSAGES)
    public ApiResponse<ChatMessageResponse> sendMessage(
            @PathVariable UUID conversationId,
            @Valid @RequestBody ChatMessageRequest request,
            @AuthenticationPrincipal User sender) {
        return ApiResponse.<ChatMessageResponse>builder()
//                .data(messageService.sendMessage(conversationId, sender, request))
                .build();
    }

    @Operation(summary = "Lấy lịch sử tin nhắn (Phân trang)")
    @GetMapping(Endpoint.Conversation.MESSAGES)
    public ApiResponse<Page<ChatMessageResponse>> getMessages(
            @PathVariable UUID conversationId,
            Pageable pageable, // ?page=0&size=20
            @AuthenticationPrincipal User currentUser) {
        return ApiResponse.<Page<ChatMessageResponse>>builder()
//                .data(messageService.getMessages(conversationId, currentUser.getId(), pageable))
                .build();
    }

    // --- SETTING CÁ NHÂN (MEMBERS) ---

    @Operation(summary = "Rời khỏi cuộc trò chuyện (Nếu là Group Chat -> Rời cả Group?)")
    @DeleteMapping(Endpoint.Conversation.MEMBERS)
    public ApiResponse<Void> leaveConversation(
            @PathVariable UUID conversationId,
            @AuthenticationPrincipal User currentUser) {
        // Logic nghiệp vụ:
        // Nếu là Direct Chat -> Ẩn hội thoại
        // Nếu là Group Chat -> Cảnh báo user phải rời Group bên Tripjoy mới đúng
//        conversationService.leaveConversation(conversationId, currentUser.getId());
        return ApiResponse.<Void>builder().message("Left conversation").build();
    }
}