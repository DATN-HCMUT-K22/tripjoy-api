package com.tripjoy.api.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tripjoy.api.dto.response.location.LocationResponse;
import com.tripjoy.api.dto.response.simple.UserSimpleResponse;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SuggestLocationResponse {

    UUID id;

    LocationResponse location;

    @JsonProperty("suggested_by")
    UserSimpleResponse user;

    @JsonProperty("group_id")
    String groupId;

    String notes;

    @JsonProperty("created_at")
    LocalDateTime createdAt;
}