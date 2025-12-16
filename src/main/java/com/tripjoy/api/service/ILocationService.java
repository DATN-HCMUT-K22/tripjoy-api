package com.tripjoy.api.service;

import com.tripjoy.api.dto.request.LocationCreateRequest;
import com.tripjoy.api.dto.response.location.LocationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for Location management (Phase 1 - Core Features)
 */
public interface ILocationService {

    /**
     * Get existing location or create new one (with duplicate prevention)
     * Checks by providerId first, then by coordinates (<50m)
     */
    LocationResponse getOrCreateLocation(LocationCreateRequest request);

    LocationResponse getLocationById(UUID locationId);

    LocationResponse updateLocation(UUID locationId, LocationCreateRequest request);

    void deleteLocation(UUID locationId);

    Page<LocationResponse> getAllLocations(Pageable pageable);

    /**
     * Find locations near a given point (Phase 2 - Nearby Search)
     * 
     * @param latitude     Current latitude
     * @param longitude    Current longitude
     * @param radiusMeters Search radius in meters (default: 5000, max: 50000)
     * @param categories   Optional POI categories filter
     * @param limit        Max results (default: 50, max: 100)
     */
    List<LocationResponse> getNearbyLocations(
            Double latitude,
            Double longitude,
            Integer radiusMeters,
            List<String> categories,
            Integer limit);

    /**
     * Search locations by text query with filters (Phase 2 - Text Search)
     * 
     * @param query      Search text (searches in name and address)
     * @param city       Filter by city name
     * @param district   Filter by district name
     * @param categories Filter by POI categories
     * @param pageable   Pagination parameters
     */
    Page<LocationResponse> searchLocations(
            String query,
            String city,
            String district,
            List<String> categories,
            Pageable pageable);
}
