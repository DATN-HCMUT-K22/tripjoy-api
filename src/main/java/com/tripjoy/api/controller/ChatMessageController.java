package com.tripjoy.api.controller;

import com.tripjoy.api.constant.Endpoint;
import com.tripjoy.api.dto.response.ApiResponse;
import com.tripjoy.api.dto.response.simple.UserSimpleResponse;
import com.tripjoy.api.service.IChatMessageService;
import com.tripjoy.api.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(Endpoint.Message.BASE)
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Messages", description = "Actions on specific messages (Like, Unlike...)")
public class ChatMessageController {

    IChatMessageService messageService;

    @Operation(summary = "Like a message")
    @PostMapping(Endpoint.Message.LIKES)
    public ApiResponse<Void> likeMessage(@PathVariable UUID messageId) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        messageService.likeMessage(messageId, currentUserId);

        return ApiResponse.<Void>builder()
                .message("Message liked")
                .build();
    }

    @Operation(summary = "Unlike a message")
    @DeleteMapping(Endpoint.Message.LIKES)
    public ApiResponse<Void> unlikeMessage(@PathVariable UUID messageId) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        messageService.unlikeMessage(messageId, currentUserId);

        return ApiResponse.<Void>builder()
                .message("Message unliked")
                .build();
    }

    @Operation(summary = "Get users who liked a message", description = "Returns a list of users who have liked the specified message")
    @GetMapping(Endpoint.Message.LIKES)
    public ApiResponse<List<UserSimpleResponse>> getMessageLikes(@PathVariable UUID messageId) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();

        return ApiResponse.<List<UserSimpleResponse>>builder()
                .data(messageService.getMessageLikes(messageId, currentUserId))
                .build();
    }

    @Operation(summary = "Pin a message in conversation")
    @PostMapping(Endpoint.Message.PIN)
    public ApiResponse<Void> pinMessage(
            @PathVariable UUID messageId,
            @RequestParam UUID conversationId) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        messageService.pinMessage(conversationId, messageId, currentUserId);

        return ApiResponse.<Void>builder()
                .message("Message pinned successfully")
                .build();
    }

    @Operation(summary = "Unpin a message from conversation")
    @DeleteMapping(Endpoint.Message.PIN)
    public ApiResponse<Void> unpinMessage(
            @PathVariable UUID messageId,
            @RequestParam UUID conversationId) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        messageService.unpinMessage(conversationId, messageId, currentUserId);

        return ApiResponse.<Void>builder()
                .message("Message unpinned successfully")
                .build();
    }
}