package com.tripjoy.api.dto.response.location;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LocationResponse {
    UUID id;
    String name;
    Double lat;
    Double lng;
    String hotline;
    String category;

    Boolean isOpen;

    String content;
}