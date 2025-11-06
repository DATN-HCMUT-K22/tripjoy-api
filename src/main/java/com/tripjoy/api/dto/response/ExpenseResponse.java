package com.tripjoy.api.dto.response;

import com.tripjoy.api.dto.response.simple.UserSimpleResponse;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ExpenseResponse {
    String id;
    String name;
    String description;
    Double amount;
    String type;
    String method;
//    String itineraryId;       itinerary info get from api controller

    UserSimpleResponse user;
}