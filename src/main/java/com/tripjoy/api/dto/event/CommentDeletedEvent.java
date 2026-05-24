package com.tripjoy.api.dto.event;

import java.util.UUID;

import com.tripjoy.api.entity.User;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CommentDeletedEvent {
    private final UUID commentId;
    private final User actor;
}
