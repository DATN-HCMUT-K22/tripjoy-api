package com.tripjoy.api.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.*;

import com.tripjoy.api.constant.Endpoint;
import com.tripjoy.api.dto.response.ApiResponse;
import com.tripjoy.api.dto.response.MessageSearchResponse;
import com.tripjoy.api.dto.response.simple.UserSimpleResponse;
import com.tripjoy.api.service.IChatMessageService;
import com.tripjoy.api.utils.SecurityUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping(Endpoint.Message.BASE)
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Messages", description = "Actions on specific messages (Like, Unlike, Search...)")
public class ChatMessageController {

    IChatMessageService messageService;

    @Operation(
            summary = "Search messages across all conversations",
            description = "Full-text search across ALL conversations the current user belongs to. "
                    + "Results are sorted by relevance and time.")
    @GetMapping(Endpoint.Message.SEARCH)
    public ApiResponse<List<MessageSearchResponse>> searchMessagesGlobal(
            @RequestParam("q") String q,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {

        UUID currentUserId = SecurityUtils.getCurrentUserId();
        return ApiResponse.<List<MessageSearchResponse>>builder()
                .data(messageService.searchMessagesGlobal(currentUserId, q, page, size))
                .build();
    }

    @Operation(summary = "Like a message")
    @PostMapping(Endpoint.Message.LIKES)
    public ApiResponse<Void> likeMessage(@PathVariable("messageId") UUID messageId) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        messageService.likeMessage(messageId, currentUserId);

        return ApiResponse.<Void>builder().message("Message liked").build();
    }

    @Operation(summary = "Unlike a message")
    @DeleteMapping(Endpoint.Message.LIKES)
    public ApiResponse<Void> unlikeMessage(@PathVariable("messageId") UUID messageId) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        messageService.unlikeMessage(messageId, currentUserId);

        return ApiResponse.<Void>builder().message("Message unliked").build();
    }

    @Operation(
            summary = "Get users who liked a message",
            description = "Returns a list of users who have liked the specified message")
    @GetMapping(Endpoint.Message.LIKES)
    public ApiResponse<List<UserSimpleResponse>> getMessageLikes(@PathVariable("messageId") UUID messageId) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();

        return ApiResponse.<List<UserSimpleResponse>>builder()
                .data(messageService.getMessageLikes(messageId, currentUserId))
                .build();
    }

    @Operation(summary = "Pin a message in conversation")
    @PostMapping(Endpoint.Message.PIN)
    public ApiResponse<Void> pinMessage(
            @PathVariable("messageId") UUID messageId, @RequestParam("conversationId") UUID conversationId) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        messageService.pinMessage(conversationId, messageId, currentUserId);

        return ApiResponse.<Void>builder()
                .message("Message pinned successfully")
                .build();
    }

    @Operation(summary = "Unpin a message from conversation")
    @DeleteMapping(Endpoint.Message.PIN)
    public ApiResponse<Void> unpinMessage(
            @PathVariable("messageId") UUID messageId, @RequestParam("conversationId") UUID conversationId) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        messageService.unpinMessage(conversationId, messageId, currentUserId);

        return ApiResponse.<Void>builder()
                .message("Message unpinned successfully")
                .build();
    }
}
