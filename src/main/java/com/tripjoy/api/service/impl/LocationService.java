package com.tripjoy.api.service.impl;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.locationtech.jts.geom.Point;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tripjoy.api.configuration.redis.RedisCacheConfig;
import com.tripjoy.api.dto.request.LocationCreateRequest;
import com.tripjoy.api.dto.request.LocationQueryParams;
import com.tripjoy.api.dto.response.location.AdministrativeLocationResponse;
import com.tripjoy.api.dto.response.location.LocationResponse;
import com.tripjoy.api.entity.Location;
import com.tripjoy.api.enums.LocationType;
import com.tripjoy.api.exception.AppException;
import com.tripjoy.api.exception.ErrorCode;
import com.tripjoy.api.mapper.LocationMapper;
import com.tripjoy.api.repository.LocationRepository;
import com.tripjoy.api.service.ILocationService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LocationService implements ILocationService {

    LocationRepository locationRepository;
    LocationMapper locationMapper;

    // ==================== Write Operations ====================

    /**
     * Get or create a location by deduplication strategy:
     * 1. By providerId (fastest — unique index)
     * 2. By adminCode + countryCode (Tier-1 administrative)
     * 3. By coordinates within 50 m (prevents near-duplicate POIs)
     * 4. Create new if no match found
     *
     * <p>Cache is NOT populated here on creation — the {@link #getLocationById}
     * path will populate on first fetch. The {@code location:provider} cache
     * is evicted if a providerId is present to avoid stale look-up results.
     */
    @Override
    @Transactional
    public LocationResponse getOrCreateLocation(LocationCreateRequest request) {
        log.debug("getOrCreateLocation: provider={}, providerId={}, name={}",
                request.getProvider(), request.getProviderId(), request.getName());

        // 1. Dedup by providerId (fastest — uses unique index)
        if (isNotBlank(request.getProviderId())) {
            Optional<Location> byProvider = locationRepository.findByProviderId(request.getProviderId());
            if (byProvider.isPresent()) {
                log.debug("Found existing location by providerId: {}", request.getProviderId());
                return locationMapper.toResponse(byProvider.get());
            }
        }

        // 2. Dedup by admin code (for Tier-1 administrative locations)
        if (isNotBlank(request.getAdminCode()) && isNotBlank(request.getCountryCode())) {
            Optional<Location> byAdminCode = locationRepository
                    .findByAdminCodeAndCountryCode(request.getAdminCode(), request.getCountryCode());
            if (byAdminCode.isPresent()) {
                log.debug("Found existing location by adminCode={}, country={}",
                        request.getAdminCode(), request.getCountryCode());
                return locationMapper.toResponse(byAdminCode.get());
            }
        }

        // 3. Dedup by coordinates (within 50m) — prevents near-duplicate POIs
        if (request.getLatitude() != null && request.getLongitude() != null) {
            Point searchPoint = locationMapper.createPoint(request.getLongitude(), request.getLatitude());
            List<Location> nearby = locationRepository.findWithin50Meters(searchPoint);
            if (!nearby.isEmpty()) {
                log.debug("Found existing location within 50m: {}", nearby.get(0).getName());
                return locationMapper.toResponse(nearby.get(0));
            }
        }

        // 4. No duplicate → create new
        Location location = locationMapper.toEntity(request);
        Location saved = locationRepository.save(location);
        log.info("Created new location: {} (type={}, lat={}, lng={})",
                saved.getName(), saved.getLocationType(), saved.getLatitude(), saved.getLongitude());

        return locationMapper.toResponse(saved);
    }

    /**
     * Update location data and evict all related cache entries.
     * Evicts both {@code location:id} and {@code location:provider} caches
     * since the location may be cached under both keys.
     */
    @Override
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = RedisCacheConfig.CACHE_LOCATION_BY_ID, key = "#locationId"),
        @CacheEvict(value = RedisCacheConfig.CACHE_LOCATION_BY_PROVIDER, allEntries = true)
    })
    public LocationResponse updateLocation(UUID locationId, LocationCreateRequest request) {
        log.info("Updating location: {}", locationId);
        Location location = findOrThrow(locationId);
        locationMapper.updateEntityFromRequest(request, location);
        Location updated = locationRepository.save(location);
        log.info("Updated location: {}", updated.getName());
        return locationMapper.toResponse(updated);
    }

    /**
     * Delete location and evict all related cache entries.
     */
    @Override
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = RedisCacheConfig.CACHE_LOCATION_BY_ID, key = "#locationId"),
        @CacheEvict(value = RedisCacheConfig.CACHE_LOCATION_BY_PROVIDER, allEntries = true)
    })
    public void deleteLocation(UUID locationId) {
        log.info("Deleting location: {}", locationId);
        Location location = findOrThrow(locationId);

        Long suggestionCount = locationRepository.countSuggestLocationsByLocationId(locationId);
        if (suggestionCount != null && suggestionCount > 0) {
            throw new AppException(ErrorCode.LOCATION_IN_USE);
        }

        locationRepository.delete(location);
        log.info("Deleted location: {}", location.getName());
    }

    /**
     * Async batch increment of {@code usage_count} for a list of location IDs.
     * Called after any user action that references locations
     * (trip item added, suggestion created, itinerary confirmed, etc.).
     *
     * <p>Cache eviction is <b>intentionally skipped</b> here: {@code usage_count}
     * affects only sort order in search results — not correctness of displayed data.
     * The cache will naturally refresh after its TTL expires (eventual consistency).
     * This avoids a cascade of cache misses on high-traffic write paths.
     */
    @Override
    @Async
    @Transactional
    public void incrementUsageCount(List<UUID> locationIds) {
        if (locationIds == null || locationIds.isEmpty())
            return;
        log.debug("Incrementing usage_count for {} locations", locationIds.size());
        locationRepository.incrementUsageCount(locationIds);
    }

    // ==================== Read Operations ====================

    /**
     * Get a single location by UUID.
     * Result is cached in {@code location:id} for 24 hours.
     */
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = RedisCacheConfig.CACHE_LOCATION_BY_ID, key = "#locationId")
    public LocationResponse getLocationById(UUID locationId) {
        log.debug("Cache MISS — loading location from DB: {}", locationId);
        return locationMapper.toResponse(findOrThrow(locationId));
    }

    /**
     * Paginated location list with optional filters and FTS.
     * NOT cached: too many parameter combinations, spatial queries change frequently.
     */
    @Override
    @Transactional(readOnly = true)
    public Page<LocationResponse> getLocations(LocationQueryParams params, Pageable pageable) {
        log.debug("getLocations: type={}, country={}, q={}, city={}",
                params.getType(), params.getCountry(), params.getQ(), params.getCity());

        String locationType = params.getType() != null ? params.getType().name() : null;

        Page<Location> result = locationRepository.searchLocations(
                nullIfBlank(params.getQ()),
                locationType,
                nullIfBlank(params.getCountry()),
                nullIfBlank(params.getCity()),
                nullIfBlank(params.getDistrict()),
                params.getLat(),
                params.getLng(),
                pageable);

        return result.map(locationMapper::toResponse);
    }

    /**
     * Get all verified administrative locations (PROVINCE, DISTRICT, etc.) by type.
     * Result cached in {@code location:admin} for 6 hours.
     * Cache key format: {@code {type}:{countryCode}} — e.g., {@code PROVINCE:VN}.
     */
    @Override
    @Transactional(readOnly = true)
    @Cacheable(
        value = RedisCacheConfig.CACHE_LOCATION_ADMIN,
        key = "#type.name() + ':' + (#countryCode != null ? #countryCode.toUpperCase() : 'ALL')"
    )
    public List<AdministrativeLocationResponse> getAdministrativeLocations(LocationType type, String countryCode) {
        log.debug("Cache MISS — loading admin locations from DB: type={}, countryCode={}", type, countryCode);

        List<Location> locations = isNotBlank(countryCode)
                ? locationRepository.findVerifiedByTypeAndCountry(type, countryCode.toUpperCase())
                : locationRepository.findVerifiedByType(type);

        return locations.stream()
                .map(locationMapper::toAdministrativeResponse)
                .toList();
    }

    /**
     * Nearby spatial search.
     * NOT cached: results are tied to precise lat/lng/radius combination — effectively uncacheable.
     */
    @Override
    @Transactional(readOnly = true)
    public List<LocationResponse> getNearbyLocations(LocationQueryParams params) {
        log.debug("getNearbyLocations: lat={}, lng={}, radius={}m",
                params.getLat(), params.getLng(), params.getRadius());

        if (!params.hasCoordinates()) {
            throw new AppException(ErrorCode.INVALID_COORDINATES);
        }

        Point searchPoint = locationMapper.createPoint(params.getLng(), params.getLat());
        String locationType = params.getType() != null ? params.getType().name() : null;

        List<Location> nearby = locationRepository.findNearby(
                searchPoint,
                params.getEffectiveRadius(),
                locationType,
                params.getEffectiveLimit());

        return nearby.stream().map(locationMapper::toResponse).toList();
    }

    /**
     * Full-text search with pagination — delegates to {@link #getLocations}.
     * NOT cached (same reasoning as getLocations).
     */
    @Override
    @Transactional(readOnly = true)
    public Page<LocationResponse> searchLocations(LocationQueryParams params, Pageable pageable) {
        return getLocations(params, pageable);
    }

    // ==================== Private Helpers ====================

    private Location findOrThrow(UUID id) {
        return locationRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.LOCATION_NOT_FOUND));
    }

    private boolean isNotBlank(String s) {
        return s != null && !s.isBlank();
    }

    private String nullIfBlank(String s) {
        return (s != null && !s.isBlank()) ? s : null;
    }
}
