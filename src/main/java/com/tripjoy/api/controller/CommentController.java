package com.tripjoy.api.controller;

import com.tripjoy.api.constant.Endpoint;
import com.tripjoy.api.dto.request.CommentRequest;
import com.tripjoy.api.dto.response.ApiResponse;
import com.tripjoy.api.dto.response.CommentResponse;
import com.tripjoy.api.service.ICommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(Endpoint.Comment.BASE)
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Comment", description = "Endpoints for managing individual comments and replies")
public class CommentController {

    ICommentService commentService;

    @Operation(summary = "Get a single comment by ID")
    @GetMapping(Endpoint.Comment.ID)
    public ApiResponse<CommentResponse> getCommentById(@PathVariable UUID commentId) {
        return ApiResponse.<CommentResponse>builder()
                // .data(commentService.getCommentById(commentId))
                .build();
    }

    @Operation(summary = "Update a comment")
    @PutMapping(Endpoint.Comment.ID)
    public ApiResponse<CommentResponse> updateComment(@PathVariable UUID commentId,
            @Valid @RequestBody CommentRequest request) {
        return ApiResponse.<CommentResponse>builder()
                // .data(commentService.updateComment(commentId, request))
                .build();
    }

    @Operation(summary = "Delete a comment")
    @DeleteMapping(Endpoint.Comment.ID)
    public ApiResponse<Void> deleteComment(@PathVariable UUID commentId) {
        // commentService.deleteComment(commentId);
        return ApiResponse.<Void>builder().message("Comment deleted successfully").build();
    }

    @Operation(summary = "Like a comment")
    @PostMapping(Endpoint.Comment.LIKES)
    public ApiResponse<Void> likeComment(@PathVariable UUID commentId) {
        // commentService.likeComment(commentId);
        return ApiResponse.<Void>builder().message("Comment liked").build();
    }

    @Operation(summary = "Unlike a comment")
    @DeleteMapping(Endpoint.Comment.LIKES)
    public ApiResponse<Void> unlikeComment(@PathVariable UUID commentId) {
        // commentService.unlikeComment(commentId);
        return ApiResponse.<Void>builder().message("Comment unliked").build();
    }

    @Operation(summary = "Get replies for a comment (paginated)")
    @GetMapping(Endpoint.Comment.REPLIES)
    public ApiResponse<Page<CommentResponse>> getRepliesForComment(
            @PathVariable UUID commentId, Pageable pageable) {
        return ApiResponse.<Page<CommentResponse>>builder()
                // .data(commentService.getRepliesForComment(commentId, pageable))
                .build();
    }

    @Operation(summary = "Create a reply for a comment")
    @PostMapping(Endpoint.Comment.REPLIES)
    public ApiResponse<CommentResponse> createReply(
            @PathVariable UUID commentId, @Valid @RequestBody CommentRequest request) {
        return ApiResponse.<CommentResponse>builder()
                // .data(commentService.createReply(commentId, request))
                .build();
    }
}