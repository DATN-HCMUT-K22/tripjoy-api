package com.tripjoy.api.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tripjoy.api.dto.response.simple.GroupSimpleResponse;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ItineraryResponse {

    UUID id;
    String name;
    String description;

    @JsonProperty("start_date")
    LocalDateTime startDate;

    @JsonProperty("end_date")
    LocalDateTime endDate;

    @JsonProperty("people_quantity")
    Integer peopleQuantity;

    @JsonProperty("budget_estimate")
    Double budgetEstimate;

    String status;

    @JsonProperty("is_favorited")
    Boolean isFavorited;

    GroupSimpleResponse group;

    Set<String> themes;

    @JsonProperty("trip_items")
    List<TripItemResponse> tripItems;
}