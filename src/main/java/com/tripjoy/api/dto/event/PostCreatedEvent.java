package com.tripjoy.api.dto.event;

import com.tripjoy.api.entity.Post;
import com.tripjoy.api.entity.User;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PostCreatedEvent {
    private final Post post;
    private final User actor;
}
