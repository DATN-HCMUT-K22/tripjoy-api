package com.tripjoy.api.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.tripjoy.api.dto.request.PostQueryParams;
import com.tripjoy.api.dto.request.PostRequest;
import com.tripjoy.api.dto.response.PostResponse;

public interface IPostService {

    /**
     * Unified post collection endpoint — list + filter + full-text search, all in one.
     *
     * <p>Replaces the previous split between {@code getAllPosts} and {@code searchPosts}.
     *
     * <p><b>Behaviour:</b>
     * <ul>
     *   <li>No params → paginated feed of all active posts (ordered by newest)
     *   <li>With {@code q} → FTS on post content
     *   <li>With {@code hashtag}, {@code creatorId}, budget ranges, etc. → multi-criteria filter
     *   <li>All filters combine as AND conditions
     * </ul>
     *
     * <p>Pagination, page size, and sort strategy are all embedded in {@link PostQueryParams}.
     *
     * @param params       filter/search criteria (all fields optional)
     * @param currentUserId caller's UUID — used to compute isLiked/isSaved flags; null for guests
     */
    Page<PostResponse> getPosts(PostQueryParams params, Pageable pageable, UUID currentUserId);

    PostResponse createPost(PostRequest request);

    PostResponse getPostById(UUID postId, UUID currentUserId);

    PostResponse updatePost(UUID postId, PostRequest request);

    void deletePost(UUID postId);

    void likePost(UUID postId);

    void unlikePost(UUID postId);

    Page<PostResponse> getSavedPosts(Pageable pageable, UUID currentUserId);

    void savePost(UUID postId);

    void unsavePost(UUID postId);
}
