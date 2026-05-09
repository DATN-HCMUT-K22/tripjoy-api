package com.tripjoy.api.service.impl;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.locationtech.jts.geom.Point;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tripjoy.api.configuration.redis.RedisCacheConfig;
import com.tripjoy.api.dto.request.LocationCreateRequest;
import com.tripjoy.api.dto.request.LocationQueryParams;
import com.tripjoy.api.dto.response.location.AdministrativeLocationResponse;
import com.tripjoy.api.dto.response.location.GoogleAutocompleteResponse;
import com.tripjoy.api.dto.response.location.LocationAutocompleteItem;
import com.tripjoy.api.dto.response.location.LocationResponse;
import com.tripjoy.api.entity.Location;
import com.tripjoy.api.enums.LocationType;
import com.tripjoy.api.exception.AppException;
import com.tripjoy.api.exception.ErrorCode;
import com.tripjoy.api.mapper.LocationMapper;
import com.tripjoy.api.repository.LocationRepository;
import com.tripjoy.api.service.IGooglePlacesService;
import com.tripjoy.api.service.ILocationService;
import com.tripjoy.api.service.ISystemConfigService;

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
    IGooglePlacesService googlePlacesService;
    ISystemConfigService configService;

    /** Max autocomplete results to return (DB + Google combined) */
    private static final int AUTOCOMPLETE_MAX_RESULTS = 10;
    /**
     * Max DB results to fetch for autocomplete — leave room for Google suggestions
     */
    private static final int AUTOCOMPLETE_DB_MAX = 5;

    // ==================== Write Operations ====================

    /**
     * Get or create a location by deduplication strategy:
     * 1. By providerId (fastest — unique index)
     * 2. By adminCode + countryCode (Tier-1 administrative)
     * 3. By coordinates within 50 m (prevents near-duplicate POIs)
     * 4. Create new if no match found
     *
     * <p>
     * Cache is NOT populated here on creation — the {@link #getLocationById}
     * path will populate on first fetch. The {@code location:provider} cache
     * is evicted if a providerId is present to avoid stale look-up results.
     */
    @Override
    @Transactional
    public LocationResponse getOrCreateLocation(LocationCreateRequest request) {
        log.debug(
                "getOrCreateLocation: provider={}, providerId={}, name={}",
                request.getProvider(),
                request.getProviderId(),
                request.getName());

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
            Optional<Location> byAdminCode =
                    locationRepository.findByAdminCodeAndCountryCode(request.getAdminCode(), request.getCountryCode());
            if (byAdminCode.isPresent()) {
                log.debug(
                        "Found existing location by adminCode={}, country={}",
                        request.getAdminCode(),
                        request.getCountryCode());
                return locationMapper.toResponse(byAdminCode.get());
            }
        }

        // 3. Dedup by coordinates (within 50m) — prevents near-duplicate POIs
        if (request.getLatitude() != null && request.getLongitude() != null) {
            Point searchPoint = locationMapper.createPoint(request.getLongitude(), request.getLatitude());
            List<Location> nearby = locationRepository.findWithin50Meters(searchPoint);
            if (!nearby.isEmpty()) {
                log.debug(
                        "Found existing location within 50m: {}", nearby.get(0).getName());
                return locationMapper.toResponse(nearby.get(0));
            }
        }

        // 4. No duplicate → create new
        Location location = locationMapper.toEntity(request);
        Location saved = locationRepository.save(location);
        log.info(
                "Created new location: {} (type={}, lat={}, lng={})",
                saved.getName(),
                saved.getLocationType(),
                saved.getLatitude(),
                saved.getLongitude());

        return locationMapper.toResponse(saved);
    }

    /**
     * Resolves a location by Google Place ID.
     * If it doesn't exist in DB, fetches from Google Places API and saves it.
     */
    @Override
    @Transactional
    public LocationResponse resolveByPlaceId(String placeId) {
        if (placeId == null || placeId.isBlank()) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "place_id is required");
        }

        // 1. Check DB first
        Optional<Location> existing = locationRepository.findByProviderId(placeId);
        if (existing.isPresent()) {
            return locationMapper.toResponse(existing.get());
        }

        // 2. Fetch from Google Places API
        log.info("Resolving missing place_id from Google Places API: {}", placeId);
        var googleDetails = googlePlacesService.getPlaceDetails(placeId).block();

        if (googleDetails == null) {
            throw new AppException(ErrorCode.LOCATION_NOT_FOUND, "Could not resolve place details from Google");
        }

        // 3. Create Location Entity
        Location location = Location.builder()
                .provider(com.tripjoy.api.enums.MapProvider.GOOGLE_MAPS)
                .providerId(googleDetails.getId())
                .name(
                        googleDetails.getDisplayName() != null
                                ? googleDetails.getDisplayName().getText()
                                : "Unknown Location")
                .fullAddress(googleDetails.getFormattedAddress())
                .poiCategories(googleDetails.getTypes())
                .primaryType(googleDetails.getPrimaryType())
                .isVerified(false)
                .usageCount(0)
                .locationType(LocationType.POI)
                .build();

        if (googleDetails.getLocation() != null
                && googleDetails.getLocation().getLatitude() != null
                && googleDetails.getLocation().getLongitude() != null) {
            location.setLatitude(googleDetails.getLocation().getLatitude());
            location.setLongitude(googleDetails.getLocation().getLongitude());
            location.setCoordinates(locationMapper.createPoint(
                    googleDetails.getLocation().getLongitude(),
                    googleDetails.getLocation().getLatitude()));
        }

        Location saved = locationRepository.save(location);
        return locationMapper.toResponse(saved);
    }

    /**
     * Update location data and evict all related cache entries.
     * Evicts both {@code location:id} and {@code location:provider} caches
     * since the location may be cached under both keys.
     */
    @Override
    @Transactional
    @Caching(
            evict = {
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
    @Caching(
            evict = {
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
     * <p>
     * Cache eviction is <b>intentionally skipped</b> here: {@code usage_count}
     * affects only sort order in search results — not correctness of displayed
     * data.
     * The cache will naturally refresh after its TTL expires (eventual
     * consistency).
     * This avoids a cascade of cache misses on high-traffic write paths.
     */
    @Override
    @Async
    @Transactional
    public void incrementUsageCount(List<UUID> locationIds) {
        if (locationIds == null || locationIds.isEmpty()) return;
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
     * NOT cached: too many parameter combinations, spatial queries change
     * frequently.
     */
    @Override
    @Transactional(readOnly = true)
    public Page<LocationResponse> getLocations(LocationQueryParams params, Pageable pageable) {
        log.debug(
                "getLocations: type={}, country={}, q={}, city={}",
                params.getType(),
                params.getCountry(),
                params.getQ(),
                params.getCity());

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
            key = "#type.name() + ':' + (#countryCode != null ? #countryCode.toUpperCase() : 'ALL')")
    public List<AdministrativeLocationResponse> getAdministrativeLocations(LocationType type, String countryCode) {
        log.debug("Cache MISS — loading admin locations from DB: type={}, countryCode={}", type, countryCode);

        List<Location> locations = isNotBlank(countryCode)
                ? locationRepository.findVerifiedByTypeAndCountry(type, countryCode.toUpperCase())
                : locationRepository.findVerifiedByType(type);

        return locations.stream()
                .map(locationMapper::toAdministrativeResponse)
                // Use collect(Collectors.toList()) instead of .toList() to ensure the result is a mutable ArrayList.
                // Immutable collections from .toList() lack a default constructor, causing Jackson deserialization
                // failures in Redis.
                .collect(Collectors.toList());
    }

    /**
     * Nearby spatial search.
     * NOT cached: results are tied to precise lat/lng/radius combination —
     * effectively uncacheable.
     */
    @Override
    @Transactional(readOnly = true)
    public List<LocationResponse> getNearbyLocations(LocationQueryParams params) {
        log.debug(
                "getNearbyLocations: lat={}, lng={}, radius={}m", params.getLat(), params.getLng(), params.getRadius());

        if (!params.hasCoordinates()) {
            throw new AppException(ErrorCode.INVALID_COORDINATES);
        }

        Point searchPoint = locationMapper.createPoint(params.getLng(), params.getLat());
        String locationType = params.getType() != null ? params.getType().name() : null;

        int defaultRadius = configService.getIntValue("SYSTEM_DEFAULT_SEARCH_RADIUS", 5000);
        int maxRadius = configService.getIntValue("SYSTEM_MAX_SEARCH_RADIUS", 50000);
        int maxLimit = configService.getIntValue("SYSTEM_MAX_PAGE_SIZE", 200);

        int effectiveRadius = (params.getRadius() == null || params.getRadius() <= 0)
                ? defaultRadius
                : Math.min(params.getRadius(), maxRadius);
        int effectiveLimit =
                (params.getLimit() == null || params.getLimit() <= 0) ? 50 : Math.min(params.getLimit(), maxLimit);

        List<Location> nearby =
                locationRepository.findNearby(searchPoint, effectiveRadius, locationType, effectiveLimit);

        return nearby.stream().map(locationMapper::toResponse).collect(Collectors.toList());
    }

    /**
     * Hybrid autocomplete — Phase 3 of the Location Architecture Blueprint.
     *
     * <p>
     * <b>Strategy:</b>
     * <ol>
     * <li>Search TripJoy DB with prefix FTS (fast, free, ranked by usage_count)
     * <li>If DB returns fewer than {@value #AUTOCOMPLETE_DB_MAX} results, call
     * Google Places Autocomplete API in parallel (blocking with 3s timeout).
     * <li>Merge results: DB items first, Google items appended — deduplicated by
     * providerId.
     * <li>Trim to {@value #AUTOCOMPLETE_MAX_RESULTS} items.
     * </ol>
     *
     * <p>
     * <b>Cache:</b> Results cached in Redis for 10 minutes keyed by {@code q:city}.
     * Short TTL because user interests (e.g., newly added POIs) change frequently.
     *
     * <p>
     * <b>Auth:</b> Public endpoint — no authentication required.
     *
     * @param q    Partial text (min 2 chars enforced at controller layer)
     * @param city Optional city bias (e.g., "Ho Chi Minh City")
     * @param lat  Optional user latitude for proximity ranking in Google API
     * @param lng  Optional user longitude for proximity ranking in Google API
     */
    @Override
    @Transactional(readOnly = true)
    @Cacheable(
            value = RedisCacheConfig.CACHE_LOCATION_AUTOCOMPLETE,
            key = "#q.toLowerCase() + ':' + (#city != null ? #city.toLowerCase() : 'all')",
            condition = "#q != null && #q.length() >= 2")
    public List<LocationAutocompleteItem> autocomplete(String q, String city, Double lat, Double lng) {
        log.debug("autocomplete: q='{}', city='{}', lat={}, lng={}", q, city, lat, lng);

        // ── Step 1: DB fast-path ──────────────────────────────────────────────
        List<LocationAutocompleteItem> dbItems = fetchFromDb(q, city, lat, lng);
        log.debug("Autocomplete DB results: {}", dbItems.size());

        // ── Step 2: Google Places fallback (if DB sparse) ────────────────────
        if (dbItems.size() >= AUTOCOMPLETE_MAX_RESULTS) {
            // DB is rich enough — skip Google API call to save quota
            return dbItems.subList(0, AUTOCOMPLETE_MAX_RESULTS);
        }

        List<LocationAutocompleteItem> googleItems = fetchFromGoogle(q, city, lat, lng, dbItems);
        log.debug("Autocomplete Google results (after dedup): {}", googleItems.size());

        // ── Step 3: Merge (DB first, Google appended) ────────────────────────
        List<LocationAutocompleteItem> merged = new ArrayList<>(dbItems);
        merged.addAll(googleItems);

        return merged.stream().limit(AUTOCOMPLETE_MAX_RESULTS).collect(Collectors.toList());
    }

    // ==================== Private Helpers ====================

    /**
     * Fetches autocomplete candidates from TripJoy's own database.
     * Uses the existing {@code searchLocations} repository query (FTS + ILIKE
     * fallback).
     */
    private List<LocationAutocompleteItem> fetchFromDb(String q, String city, Double lat, Double lng) {
        try {
            String locationType = null; // search all types
            Page<Location> dbPage = locationRepository.searchLocations(
                    nullIfBlank(q),
                    locationType,
                    null,
                    nullIfBlank(city),
                    null,
                    lat,
                    lng,
                    PageRequest.of(0, AUTOCOMPLETE_DB_MAX));

            return dbPage.getContent().stream().map(this::toAutocompleteItem).collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("DB autocomplete failed, continuing with Google only: {}", e.getMessage());
            return List.of();
        }
    }

    /**
     * Fetches autocomplete suggestions from Google Places API.
     * Deduplicates against existing DB results by providerId.
     * Returns empty list on API failure — graceful degradation.
     */
    private List<LocationAutocompleteItem> fetchFromGoogle(
            String q, String city, Double lat, Double lng, List<LocationAutocompleteItem> existingDbItems) {
        try {
            // Collect already-seen providerIds to deduplicate Google results
            Set<String> seenProviderIds = existingDbItems.stream()
                    .filter(item -> item.getProviderId() != null)
                    .map(LocationAutocompleteItem::getProviderId)
                    .collect(Collectors.toCollection(LinkedHashSet::new));

            GoogleAutocompleteResponse googleResponse =
                    googlePlacesService.autocomplete(q, city, lat, lng).block(Duration.ofSeconds(3));

            if (googleResponse == null || googleResponse.getSuggestions() == null) {
                return List.of();
            }

            return googleResponse.getSuggestions().stream()
                    .filter(s -> s.getPlacePrediction() != null)
                    .map(GoogleAutocompleteResponse.Suggestion::getPlacePrediction)
                    .filter(p -> !seenProviderIds.contains(p.getPlaceId())) // deduplicate
                    .map(this::googlePredictionToAutocompleteItem)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.warn("Google Places autocomplete failed, returning DB-only results: {}", e.getMessage());
            return List.of();
        }
    }

    /**
     * Converts a TripJoy {@link Location} entity to a slim autocomplete item (DB
     * source).
     */
    private LocationAutocompleteItem toAutocompleteItem(Location location) {
        return LocationAutocompleteItem.builder()
                .locationId(location.getId() != null ? location.getId().toString() : null)
                .providerId(location.getProviderId())
                .name(location.getName())
                .secondaryText(buildSecondaryText(location))
                .fullAddress(location.getFullAddress())
                .latitude(location.getLatitude())
                .longitude(location.getLongitude())
                .maki(location.getMaki())
                .primaryType(location.getPrimaryType())
                .source("DB")
                .build();
    }

    /**
     * Converts a Google Places prediction to an autocomplete item (GOOGLE_MAPS
     * source).
     */
    private LocationAutocompleteItem googlePredictionToAutocompleteItem(
            GoogleAutocompleteResponse.PlacePrediction prediction) {

        String mainText = prediction.getStructuredFormat() != null
                        && prediction.getStructuredFormat().getMainText() != null
                ? prediction.getStructuredFormat().getMainText().getText()
                : (prediction.getText() != null ? prediction.getText().getText() : null);

        String secondaryText = prediction.getStructuredFormat() != null
                        && prediction.getStructuredFormat().getSecondaryText() != null
                ? prediction.getStructuredFormat().getSecondaryText().getText()
                : null;

        // Infer maki icon from Google place types
        String maki = inferMaki(prediction.getTypes());
        String primaryType =
                prediction.getTypes() != null && !prediction.getTypes().isEmpty()
                        ? prediction.getTypes().get(0)
                        : null;

        return LocationAutocompleteItem.builder()
                .locationId(null) // NOT in DB yet — frontend must call POST /resolve
                .providerId(prediction.getPlaceId())
                .name(mainText)
                .secondaryText(secondaryText)
                .maki(maki)
                .primaryType(primaryType)
                .source("GOOGLE_MAPS")
                .build();
    }

    /**
     * Builds a short human-readable secondary text from a Location entity.
     * Priority: district → city → country.
     */
    private String buildSecondaryText(Location location) {
        if (location.getAddressComponents() == null) {
            return location.getPlaceFormatted() != null ? location.getPlaceFormatted() : null;
        }
        var addr = location.getAddressComponents();
        String district = addr.getEffectiveDistrict();
        String city = addr.getEffectiveCity();
        String country = addr.getCountryName();

        List<String> parts = new ArrayList<>();
        if (isNotBlank(district) && !district.equals(city)) parts.add(district);
        if (isNotBlank(city)) parts.add(city);
        if (isNotBlank(country)) parts.add(country);
        return parts.isEmpty() ? null : String.join(", ", parts);
    }

    /**
     * Maps Google place types to Maki icon names.
     * Maki icons: https://labs.mapbox.com/maki-icons/
     */
    private String inferMaki(List<String> types) {
        if (types == null || types.isEmpty()) return "marker";
        for (String type : types) {
            switch (type) {
                case "restaurant", "food" -> {
                    return "restaurant";
                }
                case "cafe", "coffee_shop" -> {
                    return "coffee";
                }
                case "lodging", "hotel" -> {
                    return "lodging";
                }
                case "airport" -> {
                    return "airport";
                }
                case "museum" -> {
                    return "museum";
                }
                case "park" -> {
                    return "park";
                }
                case "hospital", "health" -> {
                    return "hospital";
                }
                case "shopping_mall", "store" -> {
                    return "shop";
                }
                case "university", "school" -> {
                    return "college";
                }
                case "tourist_attraction" -> {
                    return "attraction";
                }
                case "bank" -> {
                    return "bank";
                }
                case "pharmacy" -> {
                    return "pharmacy";
                }
                case "gas_station" -> {
                    return "fuel";
                }
                case "locality", "administrative_area_level_1" -> {
                    return "city";
                }
            }
        }
        return "marker";
    }

    private Location findOrThrow(UUID id) {
        return locationRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.LOCATION_NOT_FOUND));
    }

    private boolean isNotBlank(String s) {
        return s != null && !s.isBlank();
    }

    private String nullIfBlank(String s) {
        return (s != null && !s.isBlank()) ? s : null;
    }
}
