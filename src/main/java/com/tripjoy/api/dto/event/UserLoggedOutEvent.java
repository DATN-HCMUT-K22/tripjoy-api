package com.tripjoy.api.dto.event;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserLoggedOutEvent {
    private final UUID userId;
}
