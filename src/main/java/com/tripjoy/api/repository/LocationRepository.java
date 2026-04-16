package com.tripjoy.api.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.locationtech.jts.geom.Point;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tripjoy.api.entity.Location;
import com.tripjoy.api.enums.LocationType;

@Repository
public interface LocationRepository extends JpaRepository<Location, UUID> {

        // ==================== Basic Finders ====================

        Optional<Location> findByProviderId(String providerId);

        List<Location> findByProviderIdIn(Collection<String> providerIds);

        Optional<Location> findByAdminCodeAndCountryCode(String adminCode, String countryCode);

        boolean existsByProviderId(String providerId);

        long countByLocationType(LocationType locationType);

        // ==================== Tier 1: Admin Locations (Province / Country / etc.)
        // ====================

        /**
         * Fetch all verified administrative locations of a given type and country.
         * Powers the high-scale GET /locations?type=PROVINCE&country=VN endpoint.
         * Uses composite index idx_location_country_type for O(log n) performance.
         */
        @Query("SELECT l FROM Location l WHERE l.locationType = :type "
                        + "AND l.countryCode = :countryCode "
                        + "AND l.isVerified = true "
                        + "ORDER BY l.usageCount DESC, l.name ASC")
        List<Location> findVerifiedByTypeAndCountry(
                        @Param("type") LocationType type,
                        @Param("countryCode") String countryCode);

        /**
         * Fetch all verified administrative locations of a given type (cross-country).
         * E.g., type=COUNTRY → all seeded countries.
         */
        @Query("SELECT l FROM Location l WHERE l.locationType = :type "
                        + "AND l.isVerified = true "
                        + "ORDER BY l.usageCount DESC, l.name ASC")
        List<Location> findVerifiedByType(@Param("type") LocationType type);

        /**
         * Unified paginated list for admin-location endpoints.
         * Applies optional country filter, useful for paginated district listings.
         */
        @Query("SELECT l FROM Location l WHERE l.locationType = :type "
                        + "AND (:countryCode IS NULL OR l.countryCode = :countryCode) "
                        + "AND l.isVerified = true "
                        + "ORDER BY l.usageCount DESC, l.name ASC")
        Page<Location> findVerifiedByTypeWithCountry(
                        @Param("type") LocationType type,
                        @Param("countryCode") String countryCode,
                        Pageable pageable);

        // ==================== Tier 2: POI Spatial Queries ====================

        /**
         * Duplicate-check: find locations within 50m of a given point.
         * Used to prevent inserting near-duplicate POIs.
         * Uses PostGIS ST_DWithin with geography cast for accurate meter-based
         * distance.
         */
        @Query(value = """
                        SELECT * FROM location
                        WHERE ST_DWithin(
                            coordinates::geography,
                            CAST(:point AS geography),
                            50
                        )
                        AND is_deleted = false
                        LIMIT 1
                        """, nativeQuery = true)
        List<Location> findWithin50Meters(@Param("point") Point point);

        /**
         * Generic radius search — ordered by distance (nearest first).
         * Used by the /nearby endpoint.
         */
        @Query(value = """
                        SELECT *,
                               ST_Distance(coordinates::geography, CAST(:point AS geography)) AS dist
                        FROM location
                        WHERE ST_DWithin(
                            coordinates::geography,
                            CAST(:point AS geography),
                            :meters
                        )
                        AND (:locationType IS NULL OR location_type = :locationType)
                        AND is_deleted = false
                        ORDER BY dist ASC
                        LIMIT :limitVal
                        """, nativeQuery = true)
        List<Location> findNearby(
                        @Param("point") Point point,
                        @Param("meters") double meters,
                        @Param("locationType") String locationType,
                        @Param("limitVal") int limit);

        /**
         * Optimized full-text search using GIN-indexed tsvector column.
         * Falls back gracefully: when search_vector is not populated , LIKE is used as
         * safety net.
         *
         * <p>
         * Ranking strategy:
         * <ol>
         * <li>FTS relevance rank (ts_rank)
         * <li>Distance from user (when lat/lng provided)
         * <li>Usage count (popularity)
         * <li>Verified locations prioritized
         * </ol>
         */
        @Query(value = """
                        SELECT l.*
                        FROM location l
                        WHERE l.is_deleted = false
                          AND (:locationType IS NULL OR l.location_type = :locationType)
                          AND (:countryCode IS NULL OR l.country_code = :countryCode)
                          AND (:city IS NULL OR LOWER(l.city) = LOWER(:city))
                          AND (:district IS NULL OR LOWER(l.admin_area_level2) = LOWER(:district))
                          AND (
                            :query IS NULL
                            OR l.search_vector @@ plainto_tsquery('simple', unaccent(:query))
                            OR lower(unaccent(l.name)) LIKE lower(unaccent(CONCAT('%', :query, '%')))
                          )
                        ORDER BY
                          l.is_verified DESC,
                          CASE WHEN :query IS NOT NULL AND l.search_vector IS NOT NULL
                               THEN ts_rank(l.search_vector, plainto_tsquery('simple', :query))
                               ELSE 0 END DESC,
                          CASE WHEN :lat IS NOT NULL AND :lng IS NOT NULL
                               THEN ST_Distance(l.coordinates::geography, ST_Point(:lng, :lat)::geography)
                               ELSE NULL END ASC,
                          l.usage_count DESC
                        """, countQuery = """
                        SELECT COUNT(*) FROM location l
                        WHERE l.is_deleted = false
                          AND (:locationType IS NULL OR l.location_type = :locationType)
                          AND (:countryCode IS NULL OR l.country_code = :countryCode)
                          AND (:city IS NULL OR LOWER(l.city) = LOWER(:city))
                          AND (:district IS NULL OR LOWER(l.admin_area_level2) = LOWER(:district))
                          AND (
                            :query IS NULL
                            OR l.search_vector @@ plainto_tsquery('simple', unaccent(:query))
                            OR lower(unaccent(l.name)) LIKE lower(unaccent(CONCAT('%', :query, '%')))
                          )
                        """, nativeQuery = true)
        Page<Location> searchLocations(
                        @Param("query") String query,
                        @Param("locationType") String locationType,
                        @Param("countryCode") String countryCode,
                        @Param("city") String city,
                        @Param("district") String district,
                        @Param("lat") Double lat,
                        @Param("lng") Double lng,
                        Pageable pageable);

        // ==================== Suggest Location count ====================

        @Query("SELECT COUNT(sl) FROM SuggestLocation sl WHERE sl.location.id = :locationId")
        Long countSuggestLocationsByLocationId(@Param("locationId") UUID locationId);

        // ==================== Usage Count (async batch update) ====================

        /**
         * Atomically increment usage_count for a batch of location IDs.
         * Called asynchronously after any operation that references locations
         * (trip item created, itinerary confirmed, post tagged, suggestion added).
         * Batch update avoids N+1 write problem.
         */
        @Modifying
        @Query("UPDATE Location l SET l.usageCount = l.usageCount + 1 WHERE l.id IN :ids")
        void incrementUsageCount(@Param("ids") List<UUID> ids);

        /**
         * Idempotent check: is the admin code already seeded for this country?
         * Used by the seeder to skip already-existing administrative locations.
         */
        @Query("SELECT COUNT(l) > 0 FROM Location l WHERE l.adminCode = :adminCode AND l.countryCode = :countryCode")
        boolean existsByAdminCodeAndCountryCode(
                        @Param("adminCode") String adminCode,
                        @Param("countryCode") String countryCode);
}
