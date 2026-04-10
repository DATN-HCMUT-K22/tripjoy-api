package com.tripjoy.api.controller;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.tripjoy.api.constant.Endpoint;
import com.tripjoy.api.dto.request.LocationCreateRequest;
import com.tripjoy.api.dto.request.LocationQueryParams;
import com.tripjoy.api.dto.response.ApiResponse;
import com.tripjoy.api.dto.response.location.AdministrativeLocationResponse;
import com.tripjoy.api.dto.response.location.LocationResponse;
import com.tripjoy.api.enums.LocationType;
import com.tripjoy.api.service.ILocationService;
import com.tripjoy.api.utils.PageableUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.concurrent.TimeUnit;

/**
 * Location API — unified, high-scale endpoint design.
 *
 * <p><b>API Design philosophy:</b>
 * <pre>
 *  GET  /api/v1/locations                → unified list
 *  GET  /api/v1/locations/administrative → verified administrative locations (provinces, countries) — cached
 *  GET  /api/v1/locations/search         → FTS + geo-aware POI search
 *  GET  /api/v1/locations/nearby         → PostGIS radius search
 *  GET  /api/v1/locations/{id}           → single location detail
 *  POST /api/v1/locations/resolve        → upsert from map autocomplete pick (public)
 *  POST /api/v1/locations                → admin manual creation
 *  PUT  /api/v1/locations/{id}           → admin update
 *  DELETE /api/v1/locations/{id}         → admin soft-delete
 * </pre>
 *
 * <p><b>Caching strategy for admin locations:</b>
 * Province and country lists change extremely rarely (< once per year).
 * We apply HTTP Cache-Control headers to allow CDN/browser/proxy caching.
 * The frontend should also cache these lists in localStorage (24h TTL).
 */
@RestController
@RequestMapping(Endpoint.Location.BASE)
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Location", description = "Location management — Two-Tier Model (Administrative + POI)")
public class LocationController {

    ILocationService locationService;

    // ==================== Tier 1: Administrative Locations (heavily cached) ====================

    /**
     * Unified administrative-location list endpoint.
     * Use this to render Dropdowns or Pickers for users to select cities/provinces/countries.
     *
     * <p>Examples:
     * <pre>
     *   GET /locations/administrative?type=PROVINCE&country=VN   → 63 VN provinces
     *   GET /locations/administrative?type=PROVINCE&country=TH   → Thai provinces
     *   GET /locations/administrative?type=COUNTRY               → all seeded countries
     *   GET /locations/administrative?type=DISTRICT&country=VN   → VN districts (if seeded)
     * </pre>
     *
     * <p>Response is HTTP-cached for 24 hours (max-age=86400).
     * ETags are managed by Spring's ShallowEtagHeaderFilter if configured.
     */
    @Operation(
            summary = "Get verified administrative locations (provinces, countries, etc.)",
            description = """
                    Returns a flat list of verified, pre-seeded administrative locations.
                    Designed for itinerary origin/destination selectors and filter chips.
                    
                    **Global scale examples:**
                    - `?type=PROVINCE&country=VN` → 63 Vietnam provinces
                    - `?type=PROVINCE&country=TH` → Thailand provinces  
                    - `?type=COUNTRY`             → all available countries
                    
                    **Performance:** Results are HTTP-cached (Cache-Control: public, max-age=86400).
                    Frontend should also cache in localStorage with 24h TTL.
                    """)
    @GetMapping(Endpoint.Location.ADMINISTRATIVE)
    public ResponseEntity<ApiResponse<List<AdministrativeLocationResponse>>> getAdministrativeLocations(
            @Parameter(description = "Location type", example = "PROVINCE")
            @RequestParam LocationType type,
            @Parameter(description = "ISO 3166-1 alpha-2 country code (omit for cross-country)", example = "VN")
            @RequestParam(required = false) String country) {

        List<AdministrativeLocationResponse> locations = locationService.getAdministrativeLocations(type, country);

        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(24, TimeUnit.HOURS).cachePublic())
                .body(ApiResponse.<List<AdministrativeLocationResponse>>builder()
                        .data(locations)
                        .build());
    }

    // ==================== Tier 2: POI Search & Nearby ====================

    /**
     * Full-text + geo-aware POI search.
     * Uses GIN-indexed tsvector for fast FTS with optional proximity ranking.
     *
     * <p>Examples:
     * <pre>
     *   GET /locations/search?q=cafe&city=HCM&page=0&size=20
     *   GET /locations/search?q=museum&country=VN&lat=10.77&lng=106.66
     *   GET /locations/search?type=POI&categories=restaurant&city=Da+Nang
     * </pre>
     */
    @Operation(
            summary = "Search locations by text and filters",
            description = """
                    Full-text search with optional filters. Uses GIN-indexed tsvector for fast FTS.
                    
                    When `lat` + `lng` are provided, results are ranked by proximity (nearest first).
                    Otherwise, ranked by FTS relevance + usage_count (popularity).
                    
                    Supports pagination via Spring `page` + `size` + `sort` parameters.
                    """)
    @GetMapping(Endpoint.Location.SEARCH)
    public ApiResponse<Page<LocationResponse>> searchLocations(
            @Parameter(description = "Text search query", example = "Highlands Coffee")
            @RequestParam(required = false) String q,
            @Parameter(description = "Location type filter", example = "POI")
            @RequestParam(required = false) LocationType type,
            @Parameter(description = "ISO 3166-1 alpha-2 country code", example = "VN")
            @RequestParam(required = false) String country,
            @Parameter(description = "City or province name", example = "Ho Chi Minh City")
            @RequestParam(required = false) String city,
            @Parameter(description = "District name", example = "Quận 1")
            @RequestParam(required = false) String district,
            @Parameter(description = "POI category filter (multi-value)", example = "cafe")
            @RequestParam(required = false) List<String> categories,
            @Parameter(description = "User latitude for proximity ranking", example = "10.762622")
            @RequestParam(required = false) Double lat,
            @Parameter(description = "User longitude for proximity ranking", example = "106.660172")
            @RequestParam(required = false) Double lng,
            Pageable pageable) {

        LocationQueryParams params = LocationQueryParams.builder()
                .q(q).type(type).country(country)
                .city(city).district(district).categories(categories)
                .lat(lat).lng(lng)
                .build();

        Pageable sqlPageable = PageableUtils.toSnakeCase(pageable);
        return ApiResponse.<Page<LocationResponse>>builder()
                .data(locationService.searchLocations(params, sqlPageable))
                .build();
    }

    /**
     * PostGIS radius-based nearby search.
     * Results ordered by distance (nearest first).
     */
    @Operation(
            summary = "Find nearby locations by coordinates",
            description = """
                    Searches for locations within a given radius using PostGIS ST_DWithin.
                    Results are ordered by distance (nearest first).
                    
                    - Default radius: 5km (5000m). Max: 50km.
                    - Default limit: 50. Max: 200.
                    - Use `type=POI` to restrict to points of interest only.
                    """)
    @GetMapping(Endpoint.Location.NEARBY)
    public ApiResponse<List<LocationResponse>> getNearbyLocations(
            @Parameter(description = "Latitude", example = "10.762622", required = true)
            @RequestParam Double lat,
            @Parameter(description = "Longitude", example = "106.660172", required = true)
            @RequestParam Double lng,
            @Parameter(description = "Radius in meters (default 5000, max 50000)", example = "5000")
            @RequestParam(required = false, defaultValue = "5000") Integer radius,
            @Parameter(description = "Location type filter", example = "POI")
            @RequestParam(required = false) LocationType type,
            @Parameter(description = "POI category filter (multi-value)", example = "cafe")
            @RequestParam(required = false) List<String> categories,
            @Parameter(description = "Max results (default 50, max 200)", example = "50")
            @RequestParam(required = false, defaultValue = "50") Integer limit) {

        LocationQueryParams params = LocationQueryParams.builder()
                .lat(lat).lng(lng).radius(radius)
                .type(type).categories(categories).limit(limit)
                .build();

        return ApiResponse.<List<LocationResponse>>builder()
                .data(locationService.getNearbyLocations(params))
                .build();
    }

    // ==================== Resolve (upsert from autocomplete pick) ====================

    /**
     * Upsert a location from a map autocomplete selection.
     * Called by the frontend immediately after the user selects a suggestion
     * from the map provider's autocomplete widget.
     *
     * <p>Backend performs triple-layer dedup (providerId → adminCode → 50m radius)
     * and returns the canonical location ID to use in trip items / suggestions.
     *
     * <p>No authentication required — this is a public flow since any user
     * can pick a location from the map.
     */
    @Operation(
            summary = "Resolve (upsert) a location from map autocomplete",
            description = """
                    Called by the frontend when a user picks a place from map autocomplete.
                    
                    Backend performs deduplication:
                    1. If `provider_id` matches an existing record → return it
                    2. If `admin_code` + `country_code` matches → return it
                    3. If coordinates are within 50m of an existing record → return it
                    4. Otherwise → create a new Location record
                    
                    Returns the canonical TripJoy location ID to use in trip_item / suggest_location.
                    """)
    @PostMapping(Endpoint.Location.RESOLVE)
    public ApiResponse<LocationResponse> resolveLocation(
            @Valid @RequestBody LocationCreateRequest request) {
        return ApiResponse.<LocationResponse>builder()
                .data(locationService.getOrCreateLocation(request))
                .build();
    }

    // ==================== CRUD ====================

    @Operation(
            summary = "[ADMIN] Create a location manually",
            description = "Admin-only. For pre-populating hot/trending locations or batch import. "
                    + "Automatically deduplicates by providerId or coordinates.")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ApiResponse<LocationResponse> createLocation(@Valid @RequestBody LocationCreateRequest request) {
        return ApiResponse.<LocationResponse>builder()
                .data(locationService.getOrCreateLocation(request))
                .build();
    }

    @Operation(summary = "[ADMIN] Update location metadata")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping(Endpoint.Location.ID)
    public ApiResponse<LocationResponse> updateLocation(
            @PathVariable UUID locationId,
            @Valid @RequestBody LocationCreateRequest request) {
        return ApiResponse.<LocationResponse>builder()
                .data(locationService.updateLocation(locationId, request))
                .build();
    }

    @Operation(
            summary = "[ADMIN] Soft-delete a location",
            description = "Marks location as deleted. Existing trips retain data; "
                    + "location will not appear in new searches.")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping(Endpoint.Location.ID)
    public ApiResponse<Void> deleteLocation(@PathVariable UUID locationId) {
        locationService.deleteLocation(locationId);
        return ApiResponse.<Void>builder()
                .message("Location deleted successfully")
                .build();
    }

    @Operation(summary = "Get a single location by ID")
    @GetMapping(Endpoint.Location.ID)
    public ApiResponse<LocationResponse> getLocationById(@PathVariable UUID locationId) {
        return ApiResponse.<LocationResponse>builder()
                .data(locationService.getLocationById(locationId))
                .build();
    }

    /**
     * Paginated list — generic endpoint. Use /administrative for verified lists,
     * /search for full-text search, /nearby for radius search.
     * This endpoint is primarily for admin tooling.
     */
    @Operation(
            summary = "[ADMIN] Paginated location list with filters",
            description = "For admin dashboards. Use /administrative, /search, or /nearby for user-facing flows.")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ApiResponse<Page<LocationResponse>> getLocations(
            @RequestParam(required = false) LocationType type,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String city,
            Pageable pageable) {

        LocationQueryParams params = LocationQueryParams.builder()
                .type(type).country(country).q(q).city(city)
                .build();

        Pageable sqlPageable = PageableUtils.toSnakeCase(pageable);
        return ApiResponse.<Page<LocationResponse>>builder()
                .data(locationService.getLocations(params, sqlPageable))
                .build();
    }
}
