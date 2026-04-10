package com.tripjoy.api.service.impl;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.ApplicationEventPublisher;

import com.tripjoy.api.dto.event.CommentCreatedEvent;
import com.tripjoy.api.dto.event.CommentLikedEvent;
import com.tripjoy.api.dto.request.CommentRequest;
import com.tripjoy.api.dto.response.CommentResponse;
import com.tripjoy.api.entity.Comment;
import com.tripjoy.api.entity.Post;
import com.tripjoy.api.entity.User;
import com.tripjoy.api.exception.AppException;
import com.tripjoy.api.exception.ErrorCode;
import com.tripjoy.api.mapper.CommentMapper;
import com.tripjoy.api.repository.CommentRepository;
import com.tripjoy.api.repository.PostRepository;
import com.tripjoy.api.repository.UserRepository;
import com.tripjoy.api.service.ICommentService;
import com.tripjoy.api.utils.SecurityUtils;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CommentService implements ICommentService {

    CommentRepository commentRepository;
    PostRepository postRepository;
    UserRepository userRepository;
    CommentMapper commentMapper;
    ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public CommentResponse createComment(CommentRequest request) {
        UUID userId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Post post = postRepository.findById(request.getPostId())
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        Comment comment = Comment.builder()
                .content(request.getContent())
                .user(user)
                .post(post)
                .isDeleted(false)
                .build();

        if (request.getParentCommentId() != null) {
            Comment parent = commentRepository.findById(request.getParentCommentId())
                    .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
            comment.setParentComment(parent);
        }

        comment = commentRepository.save(comment);

        eventPublisher.publishEvent(new CommentCreatedEvent(comment, user));

        return getCommentResponseWithContext(comment, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CommentResponse> getCommentsByPostId(UUID postId, Pageable pageable, UUID currentUserId) {
        return commentRepository.findByPostIdAndParentCommentIsNullAndIsDeletedFalse(postId, pageable)
                .map(comment -> getCommentResponseWithContext(comment, currentUserId));
    }

    @Override
    @Transactional
    public void likeComment(UUID commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        User user = userRepository.findById(SecurityUtils.getCurrentUserId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        comment.getLikeUsers().add(user);
        commentRepository.save(comment);

        eventPublisher.publishEvent(new CommentLikedEvent(comment, user));
    }

    @Override
    @Transactional
    public void unlikeComment(UUID commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        User user = userRepository.findById(SecurityUtils.getCurrentUserId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        comment.getLikeUsers().remove(user);
        commentRepository.save(comment);
    }

    @Override
    @Transactional
    public void deleteComment(UUID commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        UUID userId = SecurityUtils.getCurrentUserId();
        if (!comment.getUser().getId().equals(userId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        comment.setIsDeleted(true);
        commentRepository.save(comment);
    }

    private CommentResponse getCommentResponseWithContext(Comment comment, UUID currentUserId) {
        CommentResponse response = commentMapper.toCommentResponse(comment);
        if (currentUserId != null) {
            response.setIsLiked(comment.getLikeUsers().stream().anyMatch(u -> u.getId().equals(currentUserId)));
        } else {
            response.setIsLiked(false);
        }
        return response;
    }
}
