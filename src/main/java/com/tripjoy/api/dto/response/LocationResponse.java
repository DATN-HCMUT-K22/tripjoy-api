package com.tripjoy.api.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LocationResponse {
    String id;
    String name;
    Double lat;
    Double lng;
    String hotline;
    String category;

    Boolean isOpen;

    String content;
}