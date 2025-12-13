package com.tripjoy.api.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tripjoy.api.dto.response.location.AddressComponentsDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

/**
 * Request DTO for creating a location
 * Should be sent from Frontend after user selects from Map API autocomplete
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LocationCreateRequest {

    @NotNull
    @Schema(description = "Map provider source", example = "MAPBOX")
    String provider; // "MAPBOX", "GOOGLE_MAPS", "MANUAL"

    @Schema(description = "Provider ID (mapbox_id or place_id)")
    @JsonProperty("provider_id")
    String providerId;

    @NotBlank
    @Schema(description = "Location name", example = "Đại Học Bách Khoa TP.HCM")
    String name;

    @NotNull
    @Schema(description = "Latitude", example = "10.77324709")
    Double latitude;

    @NotNull
    @Schema(description = "Longitude", example = "106.65976722")
    Double longitude;

    @Schema(description = "Full formatted address")
    @JsonProperty("full_address")
    String fullAddress;

    @Schema(description = "Place-level formatted address")
    @JsonProperty("place_formatted")
    String placeFormatted;

    // Address components
    @Schema(description = "Structured address components")
    @JsonProperty("address_components")
    AddressComponentsDto addressComponents;

    // Categories
    @Schema(description = "POI categories", example = "[\"education\", \"university\"]")
    @JsonProperty("poi_categories")
    List<String> poiCategories;

    @Schema(description = "Maki icon name", example = "school")
    String maki;

    // Routable point
    @Schema(description = "Routable point latitude (for navigation)")
    @JsonProperty("routable_latitude")
    Double routableLatitude;

    @Schema(description = "Routable point longitude (for navigation)")
    @JsonProperty("routable_longitude")
    Double routableLongitude;

    // Optional fields
    @Schema(description = "Contact hotline")
    String hotline;

    @Schema(description = "Operational status", example = "ACTIVE")
    @JsonProperty("operational_status")
    String operationalStatus;

    @Schema(description = "Wheelchair accessible")
    @JsonProperty("wheelchair_accessible")
    Boolean wheelchairAccessible;

    // Store raw response for backup
    @Schema(description = "Raw JSON response from Map API (for backup)")
    @JsonProperty("raw_map_response")
    String rawMapResponse;
}
