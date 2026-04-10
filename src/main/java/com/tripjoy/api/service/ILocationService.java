package com.tripjoy.api.service;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.tripjoy.api.dto.request.LocationCreateRequest;
import com.tripjoy.api.dto.request.LocationQueryParams;
import com.tripjoy.api.dto.response.location.AdministrativeLocationResponse;
import com.tripjoy.api.dto.response.location.LocationResponse;
import com.tripjoy.api.enums.LocationType;

/**
 * Location service contract — provider-agnostic, high-scale.
 *
 * <p>Two main resolution flows:
 * <ol>
 *   <li>Admin / Seed flow: {@link #getOrCreateLocation} — upserts a location from map API data.
 *   <li>Query flow: {@link #getLocations} / {@link #searchLocations} — fetch with rich filtering.
 * </ol>
 */
public interface ILocationService {

    // ==================== Write Operations ====================

    /**
     * Idempotent upsert: get existing location or create a new one.
     * Deduplication order: providerId → coordinates (< 50m radius).
     * Called by:
     * - Admin manual creation (POST /locations)
     * - Resolve flow when user picks from autocomplete (POST /locations/resolve)
     * - AI itinerary generation (internal)
     */
    LocationResponse getOrCreateLocation(LocationCreateRequest request);

    LocationResponse updateLocation(UUID locationId, LocationCreateRequest request);

    void deleteLocation(UUID locationId);

    /**
     * Async batch increment of usage_count for a list of location IDs.
     * Called after trip items, itineraries, suggestions, or posts reference locations.
     */
    void incrementUsageCount(List<UUID> locationIds);

    // ==================== Read Operations ====================

    LocationResponse getLocationById(UUID locationId);

    /**
     * Unified listing endpoint — filters by type, country, city, text, coordinates.
     * Supports pagination. Powers GET /api/v1/locations.
     */
    Page<LocationResponse> getLocations(LocationQueryParams params, Pageable pageable);

    /**
     * Fetch all verified administrative locations of a given type for a country.
     * Returns a flat list (no pagination) — suitable for cached dropdown responses.
     * Example: type=PROVINCE, countryCode=VN → 63 Vietnam provinces.
     */
    List<AdministrativeLocationResponse> getAdministrativeLocations(LocationType type, String countryCode);

    /**
     * Nearby POI search using PostGIS ST_DWithin.
     * Results ordered by distance (nearest first).
     */
    List<LocationResponse> getNearbyLocations(LocationQueryParams params);

    /**
     * Full-text search with optional filters and location-aware ranking.
     */
    Page<LocationResponse> searchLocations(LocationQueryParams params, Pageable pageable);
}
