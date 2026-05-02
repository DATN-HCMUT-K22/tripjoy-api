package com.tripjoy.api.dto.response;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tripjoy.api.enums.ItineraryStatus;
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
public class ItineraryResponse extends BaseResponse {

    UUID id;

    String title;

    String description;

    @JsonProperty("start_date")
    LocalDate startDate;

    @JsonProperty("end_date")
    LocalDate endDate;

    ItineraryStatus status;

    @JsonProperty("group_id")
    UUID groupId;

    @JsonProperty("created_by_user") // Renamed
    UserSimpleResponse createdByUser;

    Set<String> themes;

    @JsonProperty("people_quantity")
    Integer peopleQuantity;
}
