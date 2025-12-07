package com.tripjoy.api.dto.response.simple;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ItinerarySimpleResponse {
    UUID id;
    String name;
}