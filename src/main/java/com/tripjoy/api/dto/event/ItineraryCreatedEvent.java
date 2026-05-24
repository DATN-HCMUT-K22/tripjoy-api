package com.tripjoy.api.dto.event;

import com.tripjoy.api.entity.Itinerary;
import com.tripjoy.api.entity.User;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ItineraryCreatedEvent {
    private final Itinerary itinerary;
    private final User actor;
}
