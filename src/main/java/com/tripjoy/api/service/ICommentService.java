package com.tripjoy.api.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.tripjoy.api.dto.request.CommentRequest;
import com.tripjoy.api.dto.response.CommentResponse;

public interface ICommentService {
    CommentResponse createComment(CommentRequest request);

    Page<CommentResponse> getCommentsByPostId(UUID postId, Pageable pageable, UUID currentUserId);

    void likeComment(UUID commentId);

    void unlikeComment(UUID commentId);

    void deleteComment(UUID commentId);

    Page<CommentResponse> getRepliesForComment(UUID commentId, Pageable pageable, UUID currentUserId);
}
