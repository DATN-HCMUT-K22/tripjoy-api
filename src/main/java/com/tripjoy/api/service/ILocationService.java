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

    List<LocationResponse> getNearbyLocations(
            Double latitude,
            Double longitude,
            Integer radiusMeters,
            List<String> categories,
            Integer limit);

    Page<LocationResponse> searchLocations(
            String query,
            String city,
            String district,
            List<String> categories,
            Pageable pageable);
}
