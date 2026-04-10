package com.tripjoy.api.dto.request;

import java.util.List;

import com.tripjoy.api.enums.LocationType;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * Query parameter object for the unified {@code GET /api/v1/locations} endpoint.
 *
 * <p>This design follows the "query object" pattern to keep the controller signature clean
 * and allow independent evolution of filter criteria without changing method signatures.
 *
 * <p><b>High-scale API design rationale:</b>
 * <pre>
 *   GET /api/v1/locations?type=PROVINCE&country=VN        → list all VN provinces
 *   GET /api/v1/locations?type=PROVINCE&country=TH        → list all Thai provinces
 *   GET /api/v1/locations?type=COUNTRY                    → list all seeded countries
 *   GET /api/v1/locations?type=POI&q=cafe&city=HCM        → search cafes in HCM
 *   GET /api/v1/locations?type=POI&lat=10.77&lng=106.66   → nearby POIs
 * </pre>
 *
 * <p>A single unified endpoint is used instead of path-per-type (e.g., /provinces, /countries)
 * because:
 * <ul>
 *   <li>Easier to extend to new LocationTypes without adding new paths
 *   <li>Frontend can use a single API client method for all location listing
 *   <li>Caching middleware (CDN / Redis) can key on full query string uniformly
 * </ul>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LocationQueryParams {

    /**
     * Filter by location type (REQUIRED for admin-location endpoints).
     * Example: type=PROVINCE, type=COUNTRY, type=POI
     */
    @Parameter(description = "Location type filter", example = "PROVINCE")
    @Schema(example = "PROVINCE")
    LocationType type;

    /**
     * Filter by ISO 3166-1 alpha-2 country code.
     * Example: country=VN, country=TH, country=JP
     * When combined with type=PROVINCE → returns all provinces of that country.
     * Global fallback: omit country to return all countries.
     */
    @Parameter(description = "ISO 3166-1 alpha-2 country code filter", example = "VN")
    String country;

    /**
     * Full-text search query (name, address).
     * Used for POI search; applied as FTS when DB has tsvector index.
     */
    @Parameter(description = "Text search query (name or address)", example = "Nha Trang")
    String q;

    /**
     * Filter POIs by city/province name (locality filter).
     * Example: city=Ho+Chi+Minh+City
     */
    @Parameter(description = "City or province name filter", example = "Ho Chi Minh City")
    String city;

    /**
     * Filter POIs by district name.
     */
    @Parameter(description = "District name filter", example = "Quận 10")
    String district;

    /**
     * Filter by one or more POI categories.
     * Example: categories=cafe&categories=restaurant
     */
    @Parameter(description = "POI category filter (multi-value)", example = "cafe")
    List<String> categories;

    /**
     * User's current latitude — enables distance-sorted results when provided.
     * Must be paired with {@code lng}.
     */
    @Parameter(description = "User latitude for proximity ranking", example = "10.762622")
    Double lat;

    /**
     * User's current longitude — enables distance-sorted results when provided.
     * Must be paired with {@code lat}.
     */
    @Parameter(description = "User longitude for proximity ranking", example = "106.660172")
    Double lng;

    /**
     * Radius in meters for nearby search.
     * Only effective when {@code lat} and {@code lng} are provided.
     * Default: 5000 (5km). Max enforced: 50000 (50km).
     */
    @Parameter(description = "Radius in meters for nearby search (default: 5000)", example = "5000")
    @Builder.Default
    Integer radius = 5000;

    /**
     * Maximum number of results to return (for list endpoints without pagination).
     * Ignored when {@code Pageable} is used.
     * Default: 50. Max enforced: 200.
     */
    @Parameter(description = "Max results limit (default 50, max 200)", example = "50")
    @Builder.Default
    Integer limit = 50;

    /**
     * If true, only return verified (admin-seeded) locations.
     * Defaults to false for POI search, true for admin-location endpoints.
     */
    @Parameter(description = "Filter to verified locations only", example = "true")
    Boolean verifiedOnly;

    // ==================== Convenience helpers ====================

    public boolean hasCoordinates() {
        return lat != null && lng != null;
    }

    public boolean hasTextQuery() {
        return q != null && !q.isBlank();
    }

    public int getEffectiveRadius() {
        if (radius == null || radius <= 0) return 5000;
        return Math.min(radius, 50_000);
    }

    public int getEffectiveLimit() {
        if (limit == null || limit <= 0) return 50;
        return Math.min(limit, 200);
    }
}
