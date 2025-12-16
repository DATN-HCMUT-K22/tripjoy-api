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

    @Override
    @Transactional
    public LocationResponse getOrCreateLocation(LocationCreateRequest request) {
        log.info("Getting or creating location: {}", request.getName());

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
        List<Location> nearbyLocations = locationRepository.findWithinDistance(searchPoint, 50.0);

        if (!nearbyLocations.isEmpty()) {
            Location existing = nearbyLocations.get(0);
            log.info("Location already exists at similar coordinates: {} (<50m away)",
                    existing.getName());
            return locationMapper.toResponse(existing);
        }

        // 3. No duplicate found → Create new location
        Location location = locationMapper.toEntity(request);
        Location saved = locationRepository.save(location);

        log.info("Created new location: {} at ({}, {})",
                saved.getName(), saved.getLatitude(), saved.getLongitude());

        return locationMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public LocationResponse getLocationById(UUID locationId) {
        log.info("Fetching location by ID: {}", locationId);

        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new AppException(ErrorCode.LOCATION_NOT_FOUND));

        return locationMapper.toResponse(location);
    }

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

    @Override
    @Transactional(readOnly = true)
    public Page<LocationResponse> getAllLocations(Pageable pageable) {
        log.info("Fetching all locations - page: {}, size: {}",
                pageable.getPageNumber(), pageable.getPageSize());

        Page<Location> locations = locationRepository.findAll(pageable);

        return locations.map(locationMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LocationResponse> getNearbyLocations(
            Double latitude,
            Double longitude,
            Integer radiusMeters,
            List<String> categories,
            Integer limit) {

        log.info("Finding nearby locations: lat={}, lng={}, radius={}m, categories={}, limit={}",
                latitude, longitude, radiusMeters, categories, limit);

        // Validation
        if (latitude == null || longitude == null) {
            throw new AppException(ErrorCode.INVALID_COORDINATES);
        }
        if (latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180) {
            throw new AppException(ErrorCode.INVALID_COORDINATES);
        }

        // Apply defaults
        int radius = (radiusMeters != null) ? radiusMeters : 5000; // Default 5km
        int maxResults = (limit != null) ? limit : 50; // Default 50

        // Enforce limits
        if (radius > 50000)
            radius = 50000; // Max 50km
        if (maxResults > 100)
            maxResults = 100; // Max 100 results

        // Create search point
        Point searchPoint = locationMapper.createPoint(longitude, latitude);

        // Query repository
        List<Location> nearbyLocations = locationRepository.findWithinDistance(searchPoint, radius);

        // Filter by categories if provided (post-query filtering since native query
        // complex)
        if (categories != null && !categories.isEmpty()) {
            nearbyLocations = nearbyLocations.stream()
                    .filter(location -> {
                        if (location.getPoiCategories() == null)
                            return false;
                        return location.getPoiCategories().stream()
                                .anyMatch(categories::contains);
                    })
                    .limit(maxResults)
                    .toList();
        } else {
            nearbyLocations = nearbyLocations.stream()
                    .limit(maxResults)
                    .toList();
        }

        log.info("Found {} nearby locations", nearbyLocations.size());
        return nearbyLocations.stream()
                .map(locationMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LocationResponse> searchLocations(
            String query,
            String city,
            String district,
            List<String> categories,
            Pageable pageable) {

        log.info("Searching locations: query='{}', city='{}', district='{}', categories={}, page={}",
                query, city, district, categories, pageable.getPageNumber());

        // Query repository (without category filter - will apply post-query)
        Page<Location> locations = locationRepository.searchLocations(
                query, city, district, pageable);

        // Apply category filter if provided (post-query)
        if (categories != null && !categories.isEmpty()) {
            List<Location> filtered = locations.getContent().stream()
                    .filter(location -> {
                        if (location.getPoiCategories() == null)
                            return false;
                        return location.getPoiCategories().stream()
                                .anyMatch(categories::contains);
                    })
                    .toList();

            log.info("Found {} locations (filtered by categories from {})",
                    filtered.size(), locations.getTotalElements());

            // Note: Page count may be inaccurate after filtering
            // For production, consider implementing category filter in query
            return locations.map(locationMapper::toResponse);
        }

        log.info("Found {} locations", locations.getTotalElements());
        return locations.map(locationMapper::toResponse);
    }
}
