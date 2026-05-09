package com.tripjoy.api.mapper;

import org.mapstruct.*;

import com.tripjoy.api.configuration.mapper.BaseMapperConfig;
import com.tripjoy.api.dto.response.CommentResponse;
import com.tripjoy.api.entity.Comment;

@Mapper(
        config = BaseMapperConfig.class,
        uses = {UserMapper.class})
public interface CommentMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "content", source = "content")
    @Mapping(target = "createdByUser", source = "user")
    @Mapping(target = "postId", source = "post.id")
    @Mapping(target = "parentCommentId", source = "parentComment.id")
    @Mapping(
            target = "likeCount",
            expression = "java((long) (comment.getLikeUsers() != null ? comment.getLikeUsers().size() : 0))")
    @Mapping(target = "replyCount", expression = "java(comment.getReplies() != null ? comment.getReplies().size() : 0)")
    @Mapping(target = "isLiked", ignore = true) // Set in service
    @Mapping(target = "latestReplies", ignore = true) // Handled in service if needed
    CommentResponse toCommentResponse(Comment comment);
}
