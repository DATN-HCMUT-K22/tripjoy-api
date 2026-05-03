package com.tripjoy.api.dto.response;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tripjoy.api.dto.response.simple.ItinerarySimpleResponse;

import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TravelNotebookResponse extends BaseResponse {

    UUID id;
    String name;

    /** Thông tin ẩm thực địa phương do AI tổng hợp */
    String food;

    /** Mô tả khí hậu + lời khuyên trang phục theo mùa */
    String climate;

    /** Văn hóa, phong tục tập quán, lễ hội */
    String culture;

    /** Số điện thoại khẩn cấp, bệnh viện, đại sứ quán */
    @JsonProperty("emergency_contacts")
    String emergencyContacts;

    // Liên kết lịch trình
    ItinerarySimpleResponse itinerary;
}
