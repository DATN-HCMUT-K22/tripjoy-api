package com.tripjoy.api.dto.ai;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.*;

/**
 * DTO gửi sang AI Service cho endpoint POST /modify-itinerary.
 * Khớp 1:1 với Python model {@code ModifyItineraryRequest}:
 *   itinerary_data      → AiFinalItineraryDto
 *   unwanted_locations  → List<AiTripItemDto>  (full TripItem objects)
 *   coordinate          → AiCoordinateDto  (tọa độ của điểm đến trong lịch trình)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiModifyItineraryRequestDto {

    @JsonProperty("itinerary_data")
    private AiFinalItineraryDto itineraryData;

    /** Danh sách TripItem đầy đủ mà user không muốn giữ. */
    @JsonProperty("unwanted_locations")
    private List<AiTripItemDto> unwantedLocations;

    /** Tọa độ điểm đến — AI dùng để search nearby places thay thế. */
    private AiCoordinateDto coordinate;
}
