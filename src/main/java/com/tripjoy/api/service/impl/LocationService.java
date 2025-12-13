package com.tripjoy.api.service.impl;

import com.tripjoy.api.dto.request.LocationCreateRequest;
import com.tripjoy.api.dto.response.location.LocationResponse;
import com.tripjoy.api.entity.Location;
import com.tripjoy.api.exception.AppException;
import com.tripjoy.api.exception.ErrorCode;
import com.tripjoy.api.mapper.LocationMapper;
import com.tripjoy.api.repository.LocationRepository;
import com.tripjoy.api.service.ILocationService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Point;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LocationService implements ILocationService {

    LocationRepository locationRepository;
    LocationMapper locationMapper;

    /**
     * Create location with duplicate prevention
     * Priority: 1. Check providerId, 2. Check coordinates proximity (<50m)
     */
    @Override
    @Transactional
    public LocationResponse createLocation(LocationCreateRequest request) {
        log.info("Creating location: {}", request.getName());

        // 1. Check duplicate by providerId (from Map API)
        if (request.getProviderId() != null && !request.getProviderId().trim().isEmpty()) {
            Optional<Location> existingByProvider = locationRepository.findByProviderId(request.getProviderId());
            if (existingByProvider.isPresent()) {
                log.info("Location already exists with providerId: {}", request.getProviderId());
                return locationMapper.toResponse(existingByProvider.get());
            }
        }

        // 2. Check duplicate by coordinates (within 50m radius)
        Point searchPoint = locationMapper.createPoint(request.getLongitude(), request.getLatitude());
        List<Location> nearbyLocations = locationRepository.findWithin50Meters(searchPoint);

        if (!nearbyLocations.isEmpty()) {
            Location existing = nearbyLocations.get(0);
            log.info("Location already exists at similar coordinates: {} ({}m away)",
                    existing.getName(), "< 50");
            return locationMapper.toResponse(existing);
        }

        // 3. No duplicate found → Create new location
        Location location = locationMapper.toEntity(request);
        Location saved = locationRepository.save(location);

        log.info("Created new location: {} at ({}, {})",
                saved.getName(), saved.getLatitude(), saved.getLongitude());

        return locationMapper.toResponse(saved);
    }

    /**
     * Get or create location (helper for SuggestLocation service)
     * Reuses createLocation logic for duplicate prevention
     */
    @Override
    @Transactional
    public LocationResponse getOrCreateLocation(LocationCreateRequest request) {
        // This delegates to createLocation which handles duplicate check
        return createLocation(request);
    }

    /**
     * Get location by ID
     */
    @Override
    @Transactional(readOnly = true)
    public LocationResponse getLocationById(UUID locationId) {
        log.info("Fetching location by ID: {}", locationId);

        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new AppException(ErrorCode.LOCATION_NOT_FOUND));

        return locationMapper.toResponse(location);
    }

    /**
     * Update location (restricted fields only)
     * Cannot update: providerId, coordinates, address_components (Map API sourced)
     * Can update: name, operational_status, hotline, wheelchair_accessible
     */
    @Override
    @Transactional
    public LocationResponse updateLocation(UUID locationId, LocationCreateRequest request) {
        log.info("Updating location: {}", locationId);

        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new AppException(ErrorCode.LOCATION_NOT_FOUND));

        // Update only allowed fields
        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            location.setName(request.getName());
        }

        if (request.getOperationalStatus() != null && !request.getOperationalStatus().trim().isEmpty()) {
            location.setOperationalStatus(
                    locationMapper.stringToOperationalStatus(request.getOperationalStatus()));
        }

        if (request.getHotline() != null) {
            location.setHotline(request.getHotline());
        }

        if (request.getWheelchairAccessible() != null) {
            location.setWheelchairAccessible(request.getWheelchairAccessible());
        }

        Location updated = locationRepository.save(location);
        log.info("Updated location: {}", updated.getName());

        return locationMapper.toResponse(updated);
    }

    /**
     * Delete location (soft delete with reference check)
     * Prevents deletion if location is referenced by SuggestLocation
     */
    @Override
    @Transactional
    public void deleteLocation(UUID locationId) {
        log.info("Deleting location: {}", locationId);

        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new AppException(ErrorCode.LOCATION_NOT_FOUND));

        // Check if location is being used in suggestions
        Long suggestionCount = locationRepository.countSuggestLocationsByLocationId(locationId);
        if (suggestionCount > 0) {
            throw new AppException(ErrorCode.LOCATION_IN_USE);
        }

        // Soft delete
        locationRepository.delete(location);
        log.info("Deleted location: {}", location.getName());
    }

    /**
     * Get all locations (paginated)
     */
    @Override
    @Transactional(readOnly = true)
    public Page<LocationResponse> getAllLocations(Pageable pageable) {
        log.info("Fetching all locations - page: {}, size: {}",
                pageable.getPageNumber(), pageable.getPageSize());

        Page<Location> locations = locationRepository.findAll(pageable);

        return locations.map(locationMapper::toResponse);
    }
}
