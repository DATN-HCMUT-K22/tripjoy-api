package com.tripjoy.api.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.tripjoy.api.dto.request.PostRequest;
import com.tripjoy.api.dto.request.PostSearchRequest;
import com.tripjoy.api.dto.response.PostResponse;

public interface IPostService {
    Page<PostResponse> searchPosts(PostSearchRequest request, UUID currentUserId);

    PostResponse createPost(PostRequest request);

    Page<PostResponse> getAllPosts(Pageable pageable, UUID currentUserId);

    PostResponse getPostById(UUID postId, UUID currentUserId);

    PostResponse updatePost(UUID postId, PostRequest request);

    void deletePost(UUID postId);

    void likePost(UUID postId);

    void unlikePost(UUID postId);

    Page<PostResponse> getSavedPosts(Pageable pageable, UUID currentUserId);

    void savePost(UUID postId);

    void unsavePost(UUID postId);
}
