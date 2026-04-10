package com.tripjoy.api.dto.request;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TripItemRequest {

    @NotNull(message = "INVALID_REQUEST")
    @JsonProperty("start_time")
    @Schema(
            name = "start_time",
            description = "Start time of this specific trip item (ISO 8601 format)",
            type = "String",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "2026-07-20T10:00:00")
    LocalDateTime startTime;

    @Schema(
            name = "duration",
            description = "Duration of the activity in minutes",
            type = "Integer",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            example = "120")
    Integer duration;
    //
    //    @NotNull
    //    @Schema(
    //            name = "day_order",
    //            description = "The day number of the trip this item belongs to (e.g., 1, 2, 3)",
    //            type = "Integer",
    //            requiredMode = Schema.RequiredMode.REQUIRED,
    //            example = "1"
    //    )
    //    Integer dayOrder;

    @Schema(
            name = "note",
            description = "A note for this trip item",
            type = "String",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            example = "Remember to buy sunscreen.")
    String note;

    @NotNull(message = "INVALID_REQUEST")
    @JsonProperty("location_id")
    @Schema(
            name = "location_id",
            description = "UUID of the Location for this trip item",
            type = "String",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "f1g2h3i4-j5k6-7890-1234-567890lmn-op")
    UUID locationId;
}
