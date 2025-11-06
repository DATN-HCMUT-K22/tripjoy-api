package com.tripjoy.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SuggestLocationRequest {

    @NotBlank
    @Schema(
            name = "location_id",
            description = "The UUID of the Location being suggested",
            type = "String",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "a1b2c3d4-e5f6-7890-1234-567890abcdef"
    )
    String locationId;

    @Schema(
            name = "notes",
            description = "Optional notes about why this location is suggested",
            type = "String",
            example = "This place looks great for our first day!"
    )
    String notes;
}