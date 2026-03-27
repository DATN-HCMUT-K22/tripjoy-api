package com.tripjoy.api.dto.request;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.tripjoy.api.enums.ItineraryStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ItineraryRequest {

    @NotBlank
    @Schema(
            name = "name",
            description = "Name of the itinerary",
            type = "String",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "Summer Trip to Da Nang")
    String name;

    @Schema(
            name = "description",
            description = "Detailed description of the itinerary",
            type = "String",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            example = "A 5-day trip to explore beaches and food in Da Nang.")
    String description;

    @NotNull
    @Schema(
            name = "start_date",
            description = "Start date and time of the trip (ISO 8601 format)",
            type = "String",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "2026-07-20T09:00:00")
    LocalDateTime startDate;

    @NotNull
    @Schema(
            name = "end_date",
            description = "End date and time of the trip (ISO 8601 format)",
            type = "String",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "2026-07-25T18:00:00")
    LocalDateTime endDate;

    @Positive(message = "People quantity must be positive")
    @Schema(
            name = "people_quantity",
            description = "Number of people in the trip",
            type = "Integer",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            example = "4")
    Integer peopleQuantity;

    @PositiveOrZero(message = "Budget must be zero or positive")
    @Schema(
            name = "budget_estimate",
            description = "Estimated budget for the trip (e.g., in VND)",
            type = "Double",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            example = "10000000")
    Double budgetEstimate;

    @Schema(
            name = "destination",
            description = "Destination of the trip",
            type = "String",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            example = "Da Nang")
    String destination;

    @Schema(
            name = "status",
            description = "Status of the itinerary",
            type = "String",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            example = "DRAFT")
    ItineraryStatus status;

    @JsonProperty("group_id")
    @Schema(
            name = "group_id",
            description = "UUID of the group this itinerary belongs to",
            type = "String",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            example = "a1b2c3d4-e5f6-7890-1234-567890abcdef")
    String groupId;

    @Schema(
            name = "themes",
            description = "Set of themes for the itinerary (from Itinerary_theme table)",
            type = "Array",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            example = "[\"Beach\", \"Food\", \"Culture\"]")
    Set<String> themes;

    // --- CASCADE CREATE ---
    // Cho phép tạo luôn các điểm đến và chi phí ngay trong payload tạo lịch trình
    @JsonProperty("trip_items")
    List<TripItemRequest> tripItems;

    @JsonProperty("expenses")
    List<ExpenseRequest> expenses;
}
