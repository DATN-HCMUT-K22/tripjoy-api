package com.tripjoy.api.dto.response;

import com.tripjoy.api.dto.response.simple.ItinerarySimpleResponse;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

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
    // createdAt từ BaseResponse (renamed từ createAt)
    ItinerarySimpleResponse itinerary;
}