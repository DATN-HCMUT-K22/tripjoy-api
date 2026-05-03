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

    /** Budget as number (VND), e.g. 10000000 */
    private Long budget;

    @JsonProperty("start_date")
    private String startDate; // YYYY-MM-DD

    @JsonProperty("end_date")
    private String endDate; // YYYY-MM-DD

    @JsonProperty("people_quantity")
    private Integer peopleQuantity;

    /**
     * Optional: list of place_ids to suggest/prefer in the generated itinerary.
     * Taken from the group's SuggestLocation list.
     */
    @JsonProperty("suggest_locations")
    private List<String> suggestLocations;
}
