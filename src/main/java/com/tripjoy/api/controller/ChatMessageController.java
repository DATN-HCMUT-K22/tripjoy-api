package com.tripjoy.api.controller;

import com.tripjoy.api.constant.Endpoint;
import com.tripjoy.api.dto.response.ApiResponse;
import com.tripjoy.api.service.IChatMessageService;
import com.tripjoy.api.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping(Endpoint.Message.BASE)
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Messages", description = "Actions on specific messages (Like, Revoke...)")
public class ChatMessageController {

    IChatMessageService messageService;

    @Operation(summary = "Like / Unlike message (Toggle) - OK")
    @PostMapping(Endpoint.Message.LIKES)
    public ApiResponse<Void> toggleLikeMessage(@PathVariable UUID messageId) {

        UUID currentUserId = SecurityUtils.getCurrentUserId();

        messageService.toggleLikeMessage(messageId, currentUserId);

        return ApiResponse.<Void>builder()
                .message("Success")
                .build();
    }
}