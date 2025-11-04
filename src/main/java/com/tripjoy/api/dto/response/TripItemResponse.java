package com.tripjoy.api.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TripItemResponse {

    String id;

    @JsonProperty("start_time")
    LocalDateTime startTime;

    Integer duration; // (in minutes)

    @JsonProperty("day_order")
    Integer dayOrder;

    String note;

    // Lồng thông tin Location, không chỉ trả về ID
    LocationResponse location;
}