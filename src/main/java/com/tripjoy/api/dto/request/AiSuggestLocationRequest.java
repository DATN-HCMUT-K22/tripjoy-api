package com.tripjoy.api.dto.request;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

import lombok.*;

/**
 * Request từ client gửi lên để AI gợi ý địa điểm thay thế cho 1 TripItem.
 * Client cung cấp itinerary ID và place_id của địa điểm cần thay thế.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiSuggestLocationRequest {

    /**
     * place_id (Google Places ID) của địa điểm cần thay thế.
     * Backend sẽ load TripItem tương ứng từ DB để gửi sang AI Service.
     */
    @NotNull(message = "Unwanted place ID is required")
    private String unwantedPlaceId;

    /**
     * Tọa độ của điểm đến — cần thiết để AI tìm địa điểm gần đó.
     * Nếu không cung cấp, backend sẽ tự suy ra từ các TripItem hiện có.
     */
    private Double latitude;
    private Double longitude;
}
