package com.tripjoy.api.dto.request;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tripjoy.api.dto.response.location.AddressComponentsDto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.FieldDefaults;

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
    @Schema(
            description = "Map provider source",
            example = "MAPBOX",
            allowableValues = {"MAPBOX", "GOOGLE_MAPS", "MANUAL"})
    String provider;

    @Schema(
            description = "Provider's unique ID (mapbox_id or Google place_id)",
            example = "dXJuOm1ieGRzdDpmNGI1MWExNi04ZTk5LTQ5YzktODI4Mi0yYjgzMzJiMmVmOWE")
    @JsonProperty("provider_id")
    String providerId;

    @NotBlank
    @Schema(description = "Location name", example = "Đại Học Bách Khoa TP.HCM")
    String name;

    @NotNull
    @Schema(description = "Latitude (WGS84)", example = "10.77324709")
    Double latitude;

    @NotNull
    @Schema(description = "Longitude (WGS84)", example = "106.65976722")
    Double longitude;

    @Schema(
            description = "Full formatted address",
            example = "268 đ. lý thường kiệt, Phường 14, Quận 10, Hồ Chí Minh, Vietnam")
    @JsonProperty("full_address")
    String fullAddress;

    @Schema(description = "Place-level formatted address", example = "Đại Học Bách Khoa TP.HCM, Quận 10, Hồ Chí Minh")
    @JsonProperty("place_formatted")
    String placeFormatted;

    // Address components
    @Schema(description = "Structured address components")
    @JsonProperty("address_components")
    AddressComponentsDto addressComponents;

    // Categories
    @Schema(description = "POI categories from map provider", example = "[\"education\", \"university\", \"school\"]")
    @JsonProperty("poi_categories")
    List<String> poiCategories;

    @Schema(description = "Maki icon identifier (Mapbox icons)", example = "school")
    String maki;

    // Routable point
    @Schema(
            description = "Routable point latitude (for navigation, may differ from main coordinates)",
            example = "10.77320000")
    @JsonProperty("routable_latitude")
    Double routableLatitude;

    @Schema(
            description = "Routable point longitude (for navigation, may differ from main coordinates)",
            example = "106.65970000")
    @JsonProperty("routable_longitude")
    Double routableLongitude;

    // Optional fields
    @Schema(description = "Contact hotline/phone number", example = "+84 28 3865 2670")
    String hotline;

    @Schema(
            description = "Operational status",
            example = "ACTIVE",
            allowableValues = {"ACTIVE", "CLOSED_PERMANENTLY", "CLOSED_TEMPORARILY", "UNKNOWN"})
    @JsonProperty("operational_status")
    String operationalStatus;

    @Schema(description = "Wheelchair accessible information", example = "true")
    @JsonProperty("wheelchair_accessible")
    Boolean wheelchairAccessible;

    // Store raw response for backup
    @Schema(
            description = "Raw JSON response from Map API (must be valid JSON object or null)",
            example =
                    "{\"mapbox_id\":\"dXJuOm1ieGRzdDpmNGI1MWExNi04ZTk5LTQ5YzktODI4Mi0yYjgzMzJiMmVmOWE\",\"feature_type\":\"poi\",\"name\":\"Đại Học Bách Khoa TP.HCM\"}",
            nullable = true)
    @JsonProperty("raw_map_response")
    String rawMapResponse;
}
