package com.tripjoy.api.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tripjoy.api.dto.response.location.LocationResponse;

import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TripItemResponse extends BaseResponse {

    UUID id;

    @JsonProperty("start_time")
    LocalDateTime startTime;

    Integer duration; // (in minutes)

    // @JsonProperty("day_order")
    // Integer dayOrder;

    String note;

    // Lồng thông tin Location, không chỉ trả về ID
    LocationResponse location;
}
