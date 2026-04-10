package com.tripjoy.api.dto.ai;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiTravelRequestDto {

    @JsonProperty("destination_name")
    private String destinationName;

    private AiCoordinateDto coordinate;

    @JsonProperty("travel_type")
    private List<String> travelType;

    private String budget;

    @JsonProperty("start_date")
    private String startDate; // AI Python model expects date string (YYYY-MM-DD)

    @JsonProperty("end_date")
    private String endDate; // AI Python model expects date string (YYYY-MM-DD)

    @JsonProperty("people_quantity")
    private Integer peopleQuantity;
}
