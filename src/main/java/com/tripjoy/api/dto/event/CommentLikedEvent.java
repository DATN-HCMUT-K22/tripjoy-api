package com.tripjoy.api.dto.event;

import com.tripjoy.api.entity.Comment;
import com.tripjoy.api.entity.User;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CommentLikedEvent {
    private final Comment comment;
    private final User actor;
}
