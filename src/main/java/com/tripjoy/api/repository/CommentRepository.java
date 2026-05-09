package com.tripjoy.api.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tripjoy.api.entity.Comment;
import com.tripjoy.api.entity.User;

@Repository
public interface CommentRepository extends JpaRepository<Comment, UUID> {
    Page<Comment> findByPostIdAndParentCommentIsNullAndIsDeletedFalse(UUID postId, Pageable pageable);

    Page<Comment> findByParentCommentIdAndIsDeletedFalseOrderByCreatedAtAsc(UUID parentCommentId, Pageable pageable);

    List<Comment> findTop2ByParentCommentIdAndIsDeletedFalseOrderByCreatedAtAsc(UUID parentCommentId);

    @Query("SELECT DISTINCT c.user FROM Comment c WHERE c.post.id = :postId AND c.parentComment IS NULL")
    List<User> findDistinctCommentersByPostId(@Param("postId") UUID postId);

    @Query("SELECT DISTINCT c.user FROM Comment c WHERE c.parentComment.id = :parentCommentId")
    List<User> findDistinctRepliersByParentCommentId(@Param("parentCommentId") UUID parentCommentId);
}
