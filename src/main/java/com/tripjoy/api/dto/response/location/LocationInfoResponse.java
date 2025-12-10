package com.tripjoy.api.dto.response.location;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LocationInfoResponse {

    UUID id;
    String content;
    String contentType;

    @JsonProperty("location_id")
    String locationId;

    @JsonProperty("updated_at")
    LocalDateTime updatedAt;
}