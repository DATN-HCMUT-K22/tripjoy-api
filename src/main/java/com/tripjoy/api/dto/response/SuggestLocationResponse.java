package com.tripjoy.api.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tripjoy.api.dto.response.location.LocationResponse;
import com.tripjoy.api.dto.response.simple.UserSimpleResponse;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SuggestLocationResponse extends BaseResponse {

    UUID id;

    LocationResponse location;

    @JsonProperty("suggested_by")
    UserSimpleResponse user;

    @JsonProperty("group_id")
    String groupId;

    String notes;

    // createdAt từ BaseResponse
}