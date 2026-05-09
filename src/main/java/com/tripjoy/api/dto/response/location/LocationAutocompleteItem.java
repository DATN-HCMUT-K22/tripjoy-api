package com.tripjoy.api.dto.response.location;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * Slim autocomplete suggestion item — designed for fast dropdown rendering.
 *
 * <p>Intentionally lean to minimize response payload during user typing (debounced calls).
 * Fields carry just enough info to render a suggestion row and resolve the location.
 *
 * <p>Two sources:
 * <ul>
 *   <li><b>DB hit</b>: {@code source = "DB"} — from TripJoy's own location table (fast, free)
 *   <li><b>Google Maps</b>: {@code source = "GOOGLE_MAPS"} — from Places Autocomplete API (real-time)
 * </ul>
 *
 * <p>Frontend flow after user picks a suggestion:
 * <ol>
 *   <li>If {@code location_id} is present → use directly (already in TripJoy DB)
 *   <li>If {@code location_id} is null → call {@code POST /locations/resolve} with
 *       the {@code provider_id} and coordinates to upsert into DB first.
 * </ol>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "LocationAutocompleteItem", description = "Autocomplete suggestion for location search")
public class LocationAutocompleteItem {

    /**
     * Internal TripJoy location ID — present only when this location already exists in DB.
     * If null, frontend must call POST /locations/resolve before using this location.
     */
    @Schema(
            description = "Internal TripJoy UUID — null means location is not yet in DB",
            example = "550e8400-e29b-41d4-a716-446655440000")
    @JsonProperty("location_id")
    String locationId;

    /**
     * Provider-specific stable place ID.
     * Google Maps: place_id (e.g., "ChIJ0T2NLikpdTERgJJ6o5gX1Kw")
     * TripJoy internal: "vn-province-79"
     * Frontend sends this back in POST /locations/resolve.
     */
    @Schema(
            description = "Provider place ID (e.g. Google place_id) — used in POST /locations/resolve",
            example = "ChIJ0T2NLikpdTERgJJ6o5gX1Kw")
    @JsonProperty("provider_id")
    String providerId;

    /** Primary display name, e.g., "Highlands Coffee - Nguyễn Huệ" */
    @Schema(description = "Primary display name", example = "Highlands Coffee - Nguyễn Huệ")
    String name;

    /**
     * Short location context — city, district, or country.
     * E.g., "Quận 1, Hồ Chí Minh" or "Vietnam"
     * Google Maps: structured_formatting.secondary_text
     */
    @Schema(description = "Short secondary location context", example = "Quận 1, Hồ Chí Minh, Vietnam")
    @JsonProperty("secondary_text")
    String secondaryText;

    /** Full formatted address for display in expanded suggestion */
    @Schema(description = "Full formatted address", example = "123 Nguyễn Huệ, Quận 1, Hồ Chí Minh")
    @JsonProperty("full_address")
    String fullAddress;

    /** Latitude — optional, may be null for Google Maps autocomplete results */
    @Schema(description = "Latitude — may be null for external autocomplete results", example = "10.7769")
    Double latitude;

    /** Longitude — optional, may be null for Google Maps autocomplete results */
    @Schema(description = "Longitude — may be null for external autocomplete results", example = "106.7009")
    Double longitude;

    /**
     * Maki icon name for rendering the marker/icon in suggestion row.
     * Defaults to "marker" for unknown types.
     */
    @Schema(description = "Maki icon identifier", example = "coffee")
    String maki;

    /**
     * Primary place type — used for icon selection fallback.
     * Google Maps: types[0] (e.g., "cafe", "restaurant", "lodging")
     */
    @Schema(description = "Primary place type for icon selection", example = "cafe")
    @JsonProperty("primary_type")
    String primaryType;

    /**
     * Source of this suggestion — for frontend analytics and resolve logic.
     * "DB" = from TripJoy database, "GOOGLE_MAPS" = from Google Places API.
     */
    @Schema(
            description = "Data source: DB or GOOGLE_MAPS",
            example = "GOOGLE_MAPS",
            allowableValues = {"DB", "GOOGLE_MAPS"})
    String source;
}
