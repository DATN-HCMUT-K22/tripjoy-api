package com.tripjoy.api.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SuggestLocationRequest {

        /**
         * Option 1: Reference existing location by ID
         * Use this if the location already exists in the database
         */
        @Schema(description = "UUID of existing location (use this OR locationData, not both)", example = "a1b2c3d4-e5f6-7890-1234-567890abcdef")
        @JsonProperty("location_id")
        UUID locationId;

        /**
         * Option 2: Create new location from Mapbox data
         * Use this when user selects from Mapbox autocomplete
         */
        @Schema(description = "New location data from Mapbox (use this OR locationId, not both)")
        @Valid
        @JsonProperty("location_data")
        LocationCreateRequest locationData;

        @Schema(description = "Optional notes about why this location is suggested", example = "This place looks great for our first day!", maxLength = 1000)
        String notes;

        /**
         * Validation: Must provide exactly ONE of locationId OR locationData, not both,
         * not neither
         */
        @AssertTrue(message = "INVALID_LOCATION_INPUT")
        private boolean isValidLocationInput() {
                boolean hasLocationId = locationId != null;
                boolean hasLocationData = locationData != null;

                // XOR: exactly one must be true
                return hasLocationId ^ hasLocationData;
        }
}
