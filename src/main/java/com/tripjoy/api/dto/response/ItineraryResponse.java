package com.tripjoy.api.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tripjoy.api.dto.response.simple.UserSimpleResponse;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.UUID;

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

    String status;

    @JsonProperty("group_id")
    UUID groupId;

    @JsonProperty("created_by_user") // Renamed
    UserSimpleResponse createdByUser;
}