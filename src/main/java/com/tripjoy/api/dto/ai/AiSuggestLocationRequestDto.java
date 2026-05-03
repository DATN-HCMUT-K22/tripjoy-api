package com.tripjoy.api.dto.ai;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.*;

/**
 * DTO gửi sang AI Service cho endpoint POST /suggest-location.
 * AI Service nhận itinerary hiện tại + 1 địa điểm không muốn giữ + tọa độ,
 * rồi trả về danh sách TripItem gợi ý thay thế cho toàn bộ itinerary.
 *
 * <p>Python model tương ứng: {@code SuggestLocationsRequest}</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiSuggestLocationRequestDto {

    /** Toàn bộ lịch trình hiện tại */
    @JsonProperty("itinerary_data")
    private AiFinalItineraryDto itineraryData;

    /** Địa điểm duy nhất mà user muốn thay thế */
    @JsonProperty("unwanted_location")
    private AiTripItemDto unwantedLocation;

    /**
     * Tọa độ của điểm đến (destination) trong itinerary —
     * dùng để AI tìm kiếm địa điểm gần đó.
     */
    private AiCoordinateDto coordinate;
}
