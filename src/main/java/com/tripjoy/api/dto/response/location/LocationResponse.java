package com.tripjoy.api.dto.response.location;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.tripjoy.api.enums.LocationType;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * Full Location response — used for detail views, POI search results, and admin endpoints.
 * Fields mirror the Google Maps Places API (Place Details) response structure.
 * Null fields are omitted from JSON output to keep responses lean.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LocationResponse {

    @Schema(description = "Internal TripJoy location ID (UUID)")
    UUID id;

    // ==================== Classification ====================

    @Schema(description = "Location type in the Two-Tier Model", example = "POI")
    @JsonProperty("location_type")
    LocationType locationType;

    @Schema(description = "Whether this location is admin-verified (e.g., pre-seeded province)", example = "false")
    @JsonProperty("is_verified")
    Boolean isVerified;

    @Schema(description = "Number of times this location has been used across the platform", example = "142")
    @JsonProperty("usage_count")
    Integer usageCount;

    // ==================== Basic Info ====================

    @Schema(description = "Primary display name", example = "Đại Học Bách Khoa TP.HCM")
    String name;

    @Schema(description = "English/International name", example = "Ho Chi Minh City University of Technology")
    @JsonProperty("name_en")
    String nameEn;

    @Schema(description = "Full formatted address from Map API", example = "268 Đ. Lý Thường Kiệt, Ho Chi Minh City, Vietnam")
    @JsonProperty("full_address")
    String fullAddress;

    @Schema(description = "Short place-level formatted address", example = "Quận 10, Hồ Chí Minh, Vietnam")
    @JsonProperty("place_formatted")
    String placeFormatted;

    // ==================== Coordinates ====================

    @Schema(description = "Latitude (WGS84)", example = "10.77324709")
    Double latitude;

    @Schema(description = "Longitude (WGS84)", example = "106.65976722")
    Double longitude;

    @Schema(description = "Routable entry-point latitude (for navigation)", example = "10.77320000")
    @JsonProperty("routable_lat")
    Double routableLatitude;

    @Schema(description = "Routable entry-point longitude (for navigation)", example = "106.65970000")
    @JsonProperty("routable_lng")
    Double routableLongitude;

    // ==================== Viewport ====================

    @Schema(description = "Recommended map viewport bounding box (JSONB)")
    String viewport;

    // ==================== Address Components ====================

    @Schema(description = "Structured address decomposition")
    @JsonProperty("address_components")
    AddressComponentsDto addressComponents;

    // ==================== Categories & UI ====================

    @Schema(description = "POI categories from map provider", example = "[\"university\",\"point_of_interest\",\"establishment\"]")
    @JsonProperty("categories")
    List<String> poiCategories;

    @Schema(description = "Most specific place type", example = "university")
    @JsonProperty("primary_type")
    String primaryType;

    @Schema(description = "Maki icon identifier (Mapbox icon set)", example = "school")
    String maki;

    @Schema(description = "Map provider icon URL")
    @JsonProperty("icon_url")
    String iconUrl;

    @Schema(description = "Icon background color hex", example = "#4DB546")
    @JsonProperty("icon_background_color")
    String iconBackgroundColor;

    // ==================== Ratings ====================

    @Schema(description = "Average user rating (1.0–5.0)", example = "4.3")
    BigDecimal rating;

    @Schema(description = "Total number of user ratings", example = "2847")
    @JsonProperty("user_ratings_total")
    Integer userRatingsTotal;

    @Schema(description = "Price level (0=free, 1=$, 2=$$, 3=$$$, 4=$$$$)", example = "2")
    @JsonProperty("price_level")
    Integer priceLevel;

    // ==================== Operational ====================

    @Schema(description = "Business/operational status", example = "OPERATIONAL")
    @JsonProperty("operational_status")
    String operationalStatus;

    @Schema(description = "Contact phone number", example = "+84 28 3865 2670")
    String hotline;

    @Schema(description = "Official website URL", example = "https://www.hcmut.edu.vn")
    String website;

    @Schema(description = "Opening hours (raw JSONB)")
    @JsonProperty("opening_hours")
    String openingHours;

    // ==================== Administrative ====================

    @Schema(description = "ISO 3166-1 alpha-2 country code", example = "VN")
    @JsonProperty("country_code")
    String countryCode;

    @Schema(description = "Admin place code (province code for VN, ISO 3166-2 for global)", example = "79")
    @JsonProperty("admin_code")
    String adminCode;

    @Schema(description = "IANA timezone identifier", example = "Asia/Ho_Chi_Minh")
    String timezone;

    // ==================== Accessibility ====================

    @Schema(description = "Wheelchair accessible", example = "true")
    @JsonProperty("wheelchair_accessible")
    Boolean wheelchairAccessible;

    // ==================== Provider ====================

    @Schema(description = "Map data provider", example = "GOOGLE_MAPS")
    String provider;

    @Schema(description = "Provider's unique place ID", example = "ChIJ0T2NLikpdTERgJJ6o5gX1Kw")
    @JsonProperty("provider_id")
    String providerId;
}
