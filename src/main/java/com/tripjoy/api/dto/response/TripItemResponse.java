package com.tripjoy.api.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tripjoy.api.dto.response.location.LocationResponse;

import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TripItemResponse extends BaseResponse {

    UUID id;

    @JsonProperty("start_time")
    LocalDateTime startTime;

    Integer duration; // (in minutes)

    // @JsonProperty("day_order")
    // Integer dayOrder;

    String note;

    // Lồng thông tin Location, không chỉ trả về ID
    LocationResponse location;

    /**
     * Tên địa điểm — dùng trong AI suggestion response khi Location chưa được lưu vào DB.
     * Sẽ null nếu đây là TripItem đã được persist (dùng location thay thế).
     */
    @JsonProperty("location_name")
    String locationName;

    /**
     * Google Place ID — dùng trong AI suggestion response.
     * Null nếu AI chưa resolve được place_id cho địa điểm này.
     */
    @JsonProperty("place_id")
    String placeId;
}
