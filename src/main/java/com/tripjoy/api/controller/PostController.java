package com.tripjoy.api.controller;

import com.tripjoy.api.constant.Endpoint;
import com.tripjoy.api.dto.request.CommentRequest;
import com.tripjoy.api.dto.request.PostRequest;
import com.tripjoy.api.dto.response.ApiResponse;
import com.tripjoy.api.dto.response.CommentResponse;
import com.tripjoy.api.dto.response.PostResponse;
import com.tripjoy.api.service.CommentService;
import com.tripjoy.api.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(Endpoint.Post.BASE)
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Post", description = "Endpoints for managing posts and interactions")
public class PostController {

    PostService postService;
    CommentService commentService; // Dùng cho nested comments

    @Operation(summary = "Create a new post")
    @PostMapping
    public ApiResponse<PostResponse> createPost(@Valid @RequestBody PostRequest request) {
        return ApiResponse.<PostResponse>builder()
//                .data(postService.createPost(request))
                .build();
    }

    @Operation(summary = "Get all posts (paginated)")
    @GetMapping
    public ApiResponse<Page<PostResponse>> getAllPosts(Pageable pageable) {
        return ApiResponse.<Page<PostResponse>>builder()
//                .data(postService.getAllPosts(pageable))
                .build();
    }

    @Operation(summary = "Get a single post by ID")
    @GetMapping(Endpoint.Post.ID)
    public ApiResponse<PostResponse> getPostById(@PathVariable String postId) {
        return ApiResponse.<PostResponse>builder()
//                .data(postService.getPostById(postId))
                .build();
    }

    @Operation(summary = "Update a post")
    @PutMapping(Endpoint.Post.ID)
    public ApiResponse<PostResponse> updatePost(@PathVariable String postId, @Valid @RequestBody PostRequest request) {
        return ApiResponse.<PostResponse>builder()
//                .data(postService.updatePost(postId, request))
                .build();
    }

    @Operation(summary = "Delete a post")
    @DeleteMapping(Endpoint.Post.ID)
    public ApiResponse<Void> deletePost(@PathVariable String postId) {
//        postService.deletePost(postId);
        return ApiResponse.<Void>builder().message("Post deleted successfully").build();
    }

    // --- Like Actions ---

    @Operation(summary = "Like a post")
    @PostMapping(Endpoint.Post.LIKES)
    public ApiResponse<Void> likePost(@PathVariable String postId) {
//        postService.likePost(postId);
        return ApiResponse.<Void>builder().message("Post liked").build();
    }

@Operation(summary = "Unlike a post")
    @DeleteMapping(Endpoint.Post.LIKES)
    public ApiResponse<Void> unlikePost(@PathVariable String postId) {
//        postService.unlikePost(postId);
        return ApiResponse.<Void>builder().message("Post unliked").build();
    }

    // --- Save Actions ---

    @Operation(summary = "Get saved posts for the current users")
    @GetMapping(Endpoint.Post.SAVES)
    public ApiResponse<PostResponse> getSavedPosts() {
        return ApiResponse.<PostResponse>builder()
//                .data(postService.getSavedPosts())
                .build();
    }

    @Operation(summary = "Save a post")
    @PostMapping(Endpoint.Post.SAVES)
    public ApiResponse<Void> savePost(@PathVariable String postId) {
//        postService.savePost(postId);
        return ApiResponse.<Void>builder().message("Post saved").build();
    }

    @Operation(summary = "Unsave a post")
    @DeleteMapping(Endpoint.Post.SAVES)
    public ApiResponse<Void> unsavePost(@PathVariable String postId) {
//        postService.unsavePost(postId);
        return ApiResponse.<Void>builder().message("Post unsaved").build();
    }


    @Operation(summary = "Get root comments for a post (paginated)")
    @GetMapping(Endpoint.Post.COMMENTS)
    // --- BẮT ĐẦU THAY ĐỔI ---
    public ApiResponse<Page<CommentResponse>> getCommentsForPost(
            @PathVariable String postId, Pageable pageable) { // <-- Thêm Pageable
        return ApiResponse.<Page<CommentResponse>>builder() // <-- Đổi List sang Page
//                .data(commentService.getRootCommentsForPost(postId, pageable))
                .build();
    }

    @Operation(summary = "Create a new root comment on a post")
    @PostMapping(Endpoint.Post.COMMENTS)
    public ApiResponse<CommentResponse> createComment(@PathVariable String postId, @Valid @RequestBody CommentRequest request) {
        return ApiResponse.<CommentResponse>builder()
//                .data(commentService.createRootComment(postId, request)) // <-- Đổi tên service cho rõ
                .build();
    }
}