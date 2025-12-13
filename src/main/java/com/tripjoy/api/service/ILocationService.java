package com.tripjoy.api.service;

import com.tripjoy.api.dto.request.LocationCreateRequest;
import com.tripjoy.api.dto.response.location.LocationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * Service interface for Location management (Phase 1 - Core Features)
 */
public interface ILocationService {

    LocationResponse createLocation(LocationCreateRequest request);

    LocationResponse getLocationById(UUID locationId);

    LocationResponse updateLocation(UUID locationId, LocationCreateRequest request);

    void deleteLocation(UUID locationId);

    Page<LocationResponse> getAllLocations(Pageable pageable);

    LocationResponse getOrCreateLocation(LocationCreateRequest request);
}
