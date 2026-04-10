package com.tripjoy.api.dto.request;

import java.math.BigDecimal;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tripjoy.api.dto.response.location.AddressComponentsDto;
import com.tripjoy.api.enums.LocationType;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * Request DTO for creating or resolving a Location.
 *
 * <p>Two main flows:
 * <ol>
 *   <li><b>User picks from Map API autocomplete</b> — Frontend collects all map API fields
 *       and sends them here. Backend upserts via {@code POST /locations/resolve}.
 *   <li><b>Admin manual creation</b> — Admin populates required fields manually
 *       via {@code POST /locations} (ADMIN only).
 * </ol>
 *
 * <p>Designed to be provider-agnostic: accepts both Google Maps and Mapbox field shapes.
 * The {@code provider} field tells the backend how to interpret {@code providerId}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LocationCreateRequest {

    // ==================== Classification ====================

    @Schema(
            description = "Location type. Defaults to POI if not provided.",
            example = "POI",
            allowableValues = {"CONTINENT", "COUNTRY", "REGION", "PROVINCE", "DISTRICT", "POI"})
    @JsonProperty("location_type")
    LocationType locationType;

    // ==================== Provider ====================

    @NotNull
    @Schema(
            description = "Map data provider.",
            example = "GOOGLE_MAPS",
            allowableValues = {"MAPBOX", "GOOGLE_MAPS", "MANUAL"})
    String provider;

    @Schema(
            description = "Provider's unique place ID. Google Maps: place_id. Mapbox: mapbox_id.",
            example = "ChIJ0T2NLikpdTERgJJ6o5gX1Kw")
    @JsonProperty("provider_id")
    String providerId;

    // ==================== Basic Info ====================

    @NotBlank
    @Schema(description = "Primary display name", example = "Đại Học Bách Khoa TP.HCM")
    String name;

    @Schema(description = "English / international name", example = "Ho Chi Minh City University of Technology")
    @JsonProperty("name_en")
    String nameEn;

    @NotNull
    @Schema(description = "Latitude (WGS84)", example = "10.77324709")
    Double latitude;

    @NotNull
    @Schema(description = "Longitude (WGS84)", example = "106.65976722")
    Double longitude;

    @Schema(
            description = "Full formatted address from Map API (Google: formatted_address)",
            example = "268 Đ. Lý Thường Kiệt, Phường 14, Quận 10, Hồ Chí Minh, Vietnam")
    @JsonProperty("full_address")
    String fullAddress;

    @Schema(description = "Short place-level formatted address (Mapbox: place_formatted)", example = "Quận 10, Hồ Chí Minh, Vietnam")
    @JsonProperty("place_formatted")
    String placeFormatted;

    // ==================== Routable Point ====================

    @Schema(description = "Routable entry-point latitude (navigation target, Mapbox: routable_points)", example = "10.77320000")
    @JsonProperty("routable_latitude")
    Double routableLatitude;

    @Schema(description = "Routable entry-point longitude", example = "106.65970000")
    @JsonProperty("routable_longitude")
    Double routableLongitude;

    // ==================== Viewport ====================

    @Schema(
            description = "Viewport bounding box as JSON string. Google Maps: geometry.viewport. Mapbox: bbox array.",
            example = "{\"northeast\":{\"lat\":10.78,\"lng\":106.67},\"southwest\":{\"lat\":10.76,\"lng\":106.65}}")
    String viewport;

    // ==================== Address Components ====================

    @Schema(description = "Structured address decomposition. Mirrors Google Maps address_components.")
    @JsonProperty("address_components")
    AddressComponentsDto addressComponents;

    // ==================== Categories & UI ====================

    @Schema(
            description = "POI type categories. Google Maps: types[]. Mapbox: categories[].",
            example = "[\"university\",\"point_of_interest\",\"establishment\"]")
    @JsonProperty("poi_categories")
    List<String> poiCategories;

    @Schema(description = "Most specific place type. Google Maps: primary_type.", example = "university")
    @JsonProperty("primary_type")
    String primaryType;

    @Schema(description = "Maki icon identifier (Mapbox icon set)", example = "school")
    String maki;

    @Schema(description = "Map provider icon URL. Google Maps: icon.", example = "https://maps.gstatic.com/mapfiles/place_api/icons/v1/png_71/school-71.png")
    @JsonProperty("icon_url")
    String iconUrl;

    @Schema(description = "Icon background color hex. Google Maps: icon_background_color.", example = "#4DB546")
    @JsonProperty("icon_background_color")
    String iconBackgroundColor;

    // ==================== Ratings ====================

    @Schema(description = "Average user rating (1.0–5.0). Google Maps: rating.", example = "4.3")
    BigDecimal rating;

    @Schema(description = "Total ratings count. Google Maps: user_ratings_total.", example = "2847")
    @JsonProperty("user_ratings_total")
    Integer userRatingsTotal;

    @Schema(description = "Price level (0–4). Google Maps: price_level.", example = "2")
    @JsonProperty("price_level")
    Integer priceLevel;

    // ==================== Operational ====================

    @Schema(
            description = "Business/operational status. Google Maps: business_status.",
            example = "OPERATIONAL",
            allowableValues = {"OPERATIONAL", "CLOSED_PERMANENTLY", "CLOSED_TEMPORARILY", "UNKNOWN"})
    @JsonProperty("operational_status")
    String operationalStatus;

    @Schema(description = "Contact phone number", example = "+84 28 3865 2670")
    String hotline;

    @Schema(description = "Official website URL. Google Maps: website.", example = "https://www.hcmut.edu.vn")
    String website;

    @Schema(
            description = "Opening hours as raw JSON. Google Maps: opening_hours.",
            example = "{\"open_now\":true,\"weekday_text\":[\"Monday: 7:30 AM – 5:00 PM\"]}")
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

    @Schema(description = "Wheelchair accessible. Google Maps: wheelchair_accessible_entrance.", example = "true")
    @JsonProperty("wheelchair_accessible")
    Boolean wheelchairAccessible;

    // ==================== Raw Response ====================

    @Schema(
            description = "Full raw JSON response from the map provider API. Stored as JSONB for forward-compatibility.",
            example = "{\"place_id\":\"ChIJ...\",\"name\":\"...\",\"types\":[...]}")
    @JsonProperty("raw_map_response")
    String rawMapResponse;
}
