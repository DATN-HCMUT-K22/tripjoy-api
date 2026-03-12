package com.tripjoy.api.dto.response;

import java.util.UUID;

import com.tripjoy.api.dto.response.simple.UserSimpleResponse;

import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ExpenseResponse extends BaseResponse {
    UUID id;
    String name;
    String description;
    Double amount;
    String type;
    String method;
    // String itineraryId; itinerary info get from api controller

    UserSimpleResponse user;
}
