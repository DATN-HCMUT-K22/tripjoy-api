package com.tripjoy.api.dto.request;

import java.util.List;

import jakarta.validation.constraints.NotNull;

import lombok.*;

/**
 * Request từ client gửi lên khi muốn AI modify itinerary.
 * Client cung cấp danh sách place_id của các TripItem không muốn đi.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiModifyItineraryRequest {

    /**
     * Danh sách place_id (Google Places ID) của các địa điểm user muốn thay thế.
     * Ví dụ: ["ChIJ...", "ChIJ..."]
     */
    @NotNull(message = "Unwanted place IDs are required")
    private List<String> unwantedPlaceIds;
}
