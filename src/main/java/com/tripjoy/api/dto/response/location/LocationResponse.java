package com.tripjoy.api.dto.response.location;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LocationResponse {

    UUID id;

    // Basic info
    String name;

    @JsonProperty("full_address")
    String fullAddress;

    @JsonProperty("place_formatted")
    String placeFormatted;

    // Coordinates
    @JsonProperty("lat")
    Double latitude;

    @JsonProperty("lng")
    Double longitude;

    @JsonProperty("routable_lat")
    Double routableLatitude;

    @JsonProperty("routable_lng")
    Double routableLongitude;

    // Address hierarchy (optional, only if needed for UI)
    @JsonProperty("address_components")
    AddressComponentsDto addressComponents;

    // Categories & UI
    @JsonProperty("categories")
    List<String> poiCategories;

    String maki;

    // Contact & Status
    String hotline;

    @JsonProperty("operational_status")
    String operationalStatus;

    // Metadata
    @JsonProperty("wheelchair_accessible")
    Boolean wheelchairAccessible;

    // Provider info (optional, for debugging)
    String provider;

    @JsonProperty("provider_id")
    String providerId;
}
