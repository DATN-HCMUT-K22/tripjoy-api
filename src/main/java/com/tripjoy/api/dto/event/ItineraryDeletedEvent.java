package com.tripjoy.api.dto.event;

import java.util.UUID;

import com.tripjoy.api.entity.User;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ItineraryDeletedEvent {
    private final UUID itineraryId;
    private final User actor;
}
