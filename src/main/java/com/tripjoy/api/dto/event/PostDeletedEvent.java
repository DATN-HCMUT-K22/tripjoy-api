package com.tripjoy.api.dto.event;

import java.util.UUID;

import com.tripjoy.api.entity.User;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PostDeletedEvent {
    private final UUID postId;
    private final User actor;
}
