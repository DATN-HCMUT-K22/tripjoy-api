package com.tripjoy.api.dto.response;

import com.tripjoy.api.dto.response.simple.UserSimpleResponse;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ExpenseResponse {
    UUID id;
    String name;
    String description;
    Double amount;
    String type;
    String method;
//    String itineraryId;       itinerary info get from api controller

    UserSimpleResponse user;
}