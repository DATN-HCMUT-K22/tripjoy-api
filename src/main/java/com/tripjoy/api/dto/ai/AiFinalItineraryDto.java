package com.tripjoy.api.dto.ai;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiFinalItineraryDto {

    private String name;

    @JsonProperty("start_date")
    private String startDate;

    @JsonProperty("end_date")
    private String endDate;

    @JsonProperty("people_quantity")
    private Integer peopleQuantity;

    /** Budget estimate as number (VND) */
    @JsonProperty("budget_estimate")
    private Long budgetEstimate;

    private List<String> themes;

    private String destination;

    @JsonProperty("trip_items")
    private List<AiTripItemDto> tripItems;
}
