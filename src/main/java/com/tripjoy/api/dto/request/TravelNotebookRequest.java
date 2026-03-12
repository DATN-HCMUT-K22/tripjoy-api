package com.tripjoy.api.dto.request;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TravelNotebookRequest {

    @NotBlank
    @Schema(
            name = "name",
            description = "Name of the travel notebook",
            type = "String",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "Da Nang Food Guide")
    String name;

    @Schema(
            name = "description",
            description = "Description or content of the notebook",
            type = "String",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            example = "List of must-try dishes and restaurants.")
    String description;

    @NotNull
    @JsonProperty("itinerary_id")
    @Schema(description = "UUID of the itinerary this notebook belongs to", requiredMode = Schema.RequiredMode.REQUIRED)
    UUID itineraryId;
}
