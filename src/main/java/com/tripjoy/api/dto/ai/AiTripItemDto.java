package com.tripjoy.api.dto.ai;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiTripItemDto {

    @JsonProperty("start_time")
    private String startTime;

    private Integer duration;

    private String note;

    @JsonProperty("location_name")
    private String locationName;

    @JsonProperty("place_id")
    private String placeId;
}
