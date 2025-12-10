package com.tripjoy.api.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tripjoy.api.dto.response.location.LocationResponse;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TripItemResponse {

    UUID id;

    @JsonProperty("start_time")
    LocalDateTime startTime;

    Integer duration; // (in minutes)

//    @JsonProperty("day_order")
//    Integer dayOrder;

    String note;

    // Lồng thông tin Location, không chỉ trả về ID
    LocationResponse location;
}