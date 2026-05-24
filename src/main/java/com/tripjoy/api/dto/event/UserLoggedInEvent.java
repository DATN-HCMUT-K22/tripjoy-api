package com.tripjoy.api.dto.event;

import com.tripjoy.api.entity.User;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserLoggedInEvent {
    private final User user;
}
