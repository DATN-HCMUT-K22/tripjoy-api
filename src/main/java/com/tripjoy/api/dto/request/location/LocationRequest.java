package com.tripjoy.api.dto.request.location;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
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
public class LocationRequest {

    @NotBlank
    @Schema(
            name = "name",
            description = "Name of the location",
            type = "String",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "My Khe Beach"
    )
    String name;

    @Schema(
            name = "lat",
            description = "Latitude of the location",
            type = "Double",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            example = "16.0544"
    )
    Double lat;

    @Schema(
            name = "lng",
            description = "Longitude of the location",
            type = "Double",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            example = "108.2208"
    )
    Double lng;

    @Schema(
            name = "hotline",
            description = "Hotline number for the location",
            type = "String",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            example = "0905123456"
    )
    String hotline;

    @Schema(
            name = "category",
            description = "Category of the location",
            type = "String",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            example = "Beach"
    )
    String category;

    @Schema(
            name = "is_open",
            description = "Is the location currently open",
            type = "Boolean",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            example = "true"
    )
    Boolean isOpen;

    @Schema(
            name = "content",
            description = "Detailed information or description of the location (from Location_info table)",
            type = "String",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            example = "One of the most beautiful beaches in Da Nang."
    )
    String content;
}
