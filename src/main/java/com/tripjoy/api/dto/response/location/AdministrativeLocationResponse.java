package com.tripjoy.api.dto.response.location;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * Slim response for administrative boundary locations (PROVINCE, COUNTRY, REGION, DISTRICT).
 * Note: "Administrative" refers to geographic boundaries, not the "Admin" user role.
 *
 * <p>Intentionally compact — used for:
 * <ul>
 *   <li>User-facing itinerary origin/destination selectors (dropdown / map picker)
 *   <li>Location filter chips in search UI
 *   <li>Cached global province lists (GET /locations/administrative?type=PROVINCE&country=VN)
 * </ul>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AdministrativeLocationResponse {

    @Schema(description = "Internal TripJoy location ID")
    UUID id;

    @Schema(description = "Location type", example = "PROVINCE")
    @JsonProperty("location_type")
    String locationType;

    @Schema(description = "Primary display name (localized)", example = "Thành phố Hồ Chí Minh")
    String name;

    @Schema(description = "English / international name", example = "Ho Chi Minh City")
    @JsonProperty("name_en")
    String nameEn;

    @Schema(description = "Latitude", example = "10.762622")
    Double latitude;

    @Schema(description = "Longitude", example = "106.660172")
    Double longitude;

    @Schema(
            description = "Recommended map viewport",
            example = "{\"northeast\":{\"lat\":11.16,\"lng\":107.03},\"southwest\":{\"lat\":10.35,\"lng\":106.36}}")
    String viewport;

    @Schema(description = "ISO 3166-1 alpha-2 country code", example = "VN")
    @JsonProperty("country_code")
    String countryCode;

    @Schema(description = "Admin place code (province code or ISO 3166-2)", example = "79")
    @JsonProperty("admin_code")
    String adminCode;

    @Schema(description = "IANA timezone", example = "Asia/Ho_Chi_Minh")
    String timezone;

    @Schema(description = "Maki icon for map marker", example = "city")
    String maki;

    @Schema(description = "Number of times referenced across the platform (popularity)", example = "3210")
    @JsonProperty("usage_count")
    Integer usageCount;

    @Schema(
            description = "Provider's unique place ID (for map SDK integration)",
            example = "ChIJo4EX8ompMTERBrgMc-blO0A")
    @JsonProperty("provider_id")
    String providerId;
}
