package com.tripjoy.api.controller;

import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import com.tripjoy.api.constant.Endpoint;
import com.tripjoy.api.dto.request.CommentRequest;
import com.tripjoy.api.dto.request.PostQueryParams;
import com.tripjoy.api.dto.request.PostRequest;
import com.tripjoy.api.dto.response.ApiResponse;
import com.tripjoy.api.dto.response.CommentResponse;
import com.tripjoy.api.dto.response.PostResponse;
import com.tripjoy.api.service.ICommentService;
import com.tripjoy.api.service.IPostService;
import com.tripjoy.api.utils.SecurityUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping(Endpoint.Post.BASE)
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Post", description = "Endpoints for managing posts and interactions")
public class PostController {

    IPostService postService;
    ICommentService commentService;

    // --- POST CRUD ---

    @Operation(summary = "Create a new post")
    @PostMapping
    public ApiResponse<PostResponse> createPost(@Valid @RequestBody PostRequest request) {
        return ApiResponse.<PostResponse>builder()
                .data(postService.createPost(request))
                .build();
    }

    /**
     * Unified GET /posts — list + filter + full-text search in a single endpoint.
     *
     * <p>All query parameters are optional. When no parameters are provided, returns a
     * paginated feed of all active posts (newest first). When any filter/search
     * parameter is provided, filtering is applied as AND conditions.
     *
     * <p>Replaces the previous split of:
     * <ul>
     *   <li>{@code GET /posts} — feed all posts
     *   <li>{@code GET /posts/search} — filtered/searched posts
     * </ul>
     *
     * <p>Pagination is handled via standard Spring {@code Pageable} params:
     * {@code ?page=0&size=20&sort=createdAt,desc}
     *
     * <p><b>Example requests:</b>
     * <pre>
     *   GET /posts                             → feed all posts
     *   GET /posts?q=Đà%20Nẵng                → FTS on content
     *   GET /posts?hashtag=dulich              → filter by hashtag
     *   GET /posts?q=cafe&hashtag=hcm&min_days=2 → combined filter
     *   GET /posts?creator_id=uuid             → posts by a user
     * </pre>
     */
    @Operation(
            summary = "Get / search posts (unified)",
            description = """
                    Unified collection endpoint. All parameters are optional:
                    - No params → paginated feed (all active posts, newest first)
                    - `q` → full-text search on post content
                    - `hashtag`, `creator_id`, budget/date/people ranges → multi-criteria filter
                    - All active filters combine as AND conditions
                    
                    Pagination: standard Spring params `page`, `size`, `sort`.
                    """)
    @GetMapping
    public ApiResponse<Page<PostResponse>> getPosts(
            @Valid @ModelAttribute PostQueryParams params,
            Pageable pageable) {
        UUID currentUserId = SecurityUtils.getCurrentUserIdSafe();
        return ApiResponse.<Page<PostResponse>>builder()
                .data(postService.getPosts(params, pageable, currentUserId))
                .build();
    }

    @Operation(summary = "Get a single post by ID")
    @GetMapping(Endpoint.Post.ID)
    public ApiResponse<PostResponse> getPostById(@PathVariable("postId") UUID postId) {
        UUID currentUserId = SecurityUtils.getCurrentUserIdSafe();
        return ApiResponse.<PostResponse>builder()
                .data(postService.getPostById(postId, currentUserId))
                .build();
    }

    @Operation(summary = "Update a post")
    @PutMapping(Endpoint.Post.ID)
    public ApiResponse<PostResponse> updatePost(@PathVariable("postId") UUID postId, @Valid @RequestBody PostRequest request) {
        return ApiResponse.<PostResponse>builder()
                .data(postService.updatePost(postId, request))
                .build();
    }

    @Operation(summary = "Delete a post")
    @DeleteMapping(Endpoint.Post.ID)
    public ApiResponse<Void> deletePost(@PathVariable("postId") UUID postId) {
        postService.deletePost(postId);
        return ApiResponse.<Void>builder().message("Post deleted successfully").build();
    }

    // --- Like Actions ---

    @Operation(summary = "Like a post")
    @PostMapping(Endpoint.Post.LIKES)
    public ApiResponse<Void> likePost(@PathVariable("postId") UUID postId) {
        postService.likePost(postId);
        return ApiResponse.<Void>builder().message("Post liked").build();
    }

    @Operation(summary = "Unlike a post")
    @DeleteMapping(Endpoint.Post.LIKES)
    public ApiResponse<Void> unlikePost(@PathVariable("postId") UUID postId) {
        postService.unlikePost(postId);
        return ApiResponse.<Void>builder().message("Post unliked").build();
    }

    // --- Save Actions ---

    @Operation(summary = "Get saved posts for the current user")
    @GetMapping(Endpoint.Post.MY_SAVES)
    public ApiResponse<Page<PostResponse>> getMySavedPosts(Pageable pageable) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        return ApiResponse.<Page<PostResponse>>builder()
                .data(postService.getSavedPosts(pageable, currentUserId))
                .build();
    }

    @Operation(summary = "Save a post")
    @PostMapping(Endpoint.Post.SAVES)
    public ApiResponse<Void> savePost(@PathVariable("postId") UUID postId) {
        postService.savePost(postId);
        return ApiResponse.<Void>builder().message("Post saved").build();
    }

    @Operation(summary = "Unsave a post")
    @DeleteMapping(Endpoint.Post.SAVES)
    public ApiResponse<Void> unsavePost(@PathVariable("postId") UUID postId) {
        postService.unsavePost(postId);
        return ApiResponse.<Void>builder().message("Post unsaved").build();
    }

    @Operation(summary = "Get root comments for a post (paginated)")
    @GetMapping(Endpoint.Post.COMMENTS)
    public ApiResponse<Page<CommentResponse>> getCommentsForPost(
            @PathVariable("postId") UUID postId, Pageable pageable) {
        UUID currentUserId = SecurityUtils.getCurrentUserIdSafe();
        return ApiResponse.<Page<CommentResponse>>builder()
                .data(commentService.getCommentsByPostId(postId, pageable, currentUserId))
                .build();
    }

    @Operation(summary = "Create a new root comment on a post")
    @PostMapping(Endpoint.Post.COMMENTS)
    public ApiResponse<CommentResponse> createComment(
            @PathVariable("postId") UUID postId, @Valid @RequestBody CommentRequest request) {
        // Force correct postId from path into request if mismatch
        request.setPostId(postId);
        return ApiResponse.<CommentResponse>builder()
                .data(commentService.createComment(request))
                .build();
    }
}
