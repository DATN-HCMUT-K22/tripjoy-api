package com.tripjoy.api.dto.response;

import com.tripjoy.api.dto.response.simple.ItinerarySimpleResponse;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TravelNotebookResponse {
    UUID id;
    String name;
    String description;
    LocalDateTime createAt;
    ItinerarySimpleResponse itinerary;
}