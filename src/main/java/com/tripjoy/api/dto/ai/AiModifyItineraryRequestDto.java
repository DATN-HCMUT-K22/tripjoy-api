package com.tripjoy.api.dto.ai;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.*;

/**
 * DTO gửi sang AI Service cho endpoint POST /modify-itinerary.
 * Khớp 1:1 với Python model {@code ModifyItineraryRequest}:
 *   itinerary_data  → AiFinalItineraryDto
 *   unwanted_locations → List<AiTripItemDto>
 *   coordinate      → AiCoordinateDto
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiModifyItineraryRequestDto {

    @JsonProperty("itinerary_data")
    private AiFinalItineraryDto itineraryData;

    @JsonProperty("unwanted_locations")
    private List<AiTripItemDto> unwantedLocations;

    private AiCoordinateDto coordinate;
}
