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
    String description;

    /** Thông tin ẩm thực địa phương do AI tổng hợp */
    String food;

    /** Mô tả khí hậu + lời khuyên trang phục theo mùa */
    String climate;

    /** Văn hóa, phong tục tập quán, lễ hội */
    String culture;

    @JsonProperty("weather_forecast")
    String weatherForecast;

    @JsonProperty("culture_etiquette")
    String cultureEtiquette;

    @JsonProperty("emergency_contacts")
    String emergencyContacts;

    @JsonProperty("packing_guide")
    String packingGuide;

    // createdAt từ BaseResponse
    ItinerarySimpleResponse itinerary;
}

