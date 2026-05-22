package com.tripjoy.api.entity;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.*;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.locationtech.jts.geom.Point;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tripjoy.api.converter.StringListConverter;
import com.tripjoy.api.entity.embeddable.AddressComponents;
import com.tripjoy.api.entity.embeddable.SoftDeleteInfo;
import com.tripjoy.api.enums.LocationType;
import com.tripjoy.api.enums.MapProvider;
import com.tripjoy.api.enums.OperationalStatus;

import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * Location entity — unified store for all geographical locations in TripJoy.
 *
 * <p><b>Two-Tier Model:</b>
 * <ul>
 *   <li><b>Administrative locations</b> (PROVINCE, DISTRICT, COUNTRY, ...):
 *       Pre-seeded, verified, used for Itinerary origin/destination.
 *   <li><b>POI — Point of Interest</b>: Lazily loaded from Map API when users search
 *       for places to add to trip items or group suggestions.
 * </ul>
 *
 * <p><b>Spatial:</b> Uses PostGIS geometry(Point, 4326) for spatial indexing and queries.
 * Dual-stores coordinates as PostGIS Point + plain Double columns for flexibility.
 *
 * <p><b>Map API coverage:</b> Fields are designed to cover the Google Maps Places API
 * (Place Details) response schema, enabling round-trip fidelity without data loss.
 * Also compatible with Mapbox Geocoding/Search API response structure.
 */
@Getter
@Setter
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "location",
        indexes = {
            @Index(name = "idx_location_provider_id", columnList = "provider_id"),
            @Index(name = "idx_location_type", columnList = "location_type"),
            @Index(name = "idx_location_type_verified", columnList = "location_type, is_verified"),
            @Index(name = "idx_location_country_type", columnList = "country_code, location_type"),
            @Index(name = "idx_location_usage_count", columnList = "usage_count DESC"),
            @Index(name = "idx_location_coordinates", columnList = "latitude, longitude")
        })
public class Location extends BaseEntity {

    // ==================== CLASSIFICATION (Two-Tier Model) ====================

    /**
     * Classifies this location in the Two-Tier Model.
     * PROVINCE: Pre-seeded province/city for itinerary selectors.
     * POI: User-discovered place from Map API.
     */
    @Column(name = "location_type", length = 30, nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private LocationType locationType = LocationType.POI;

    /**
     * Admin-verified locations (e.g., pre-seeded provinces) are flagged true.
     * Unverified POIs added by users start as false.
     * Verified locations are prioritized in search results.
     */
    @Column(name = "is_verified", nullable = false)
    @Builder.Default
    private Boolean isVerified = false;

    /**
     * Number of times this location has been referenced across the system
     * (trip items, itinerary origins/destinations, suggest-locations, posts).
     * Used for popularity ranking in search results and autocomplete.
     * Updated via async batch jobs to avoid write contention.
     */
    @Column(name = "usage_count", nullable = false)
    @Builder.Default
    private Integer usageCount = 0;

    // ==================== PROVIDER INFO ====================

    /** Map data provider for this record. */
    @Column(name = "provider", length = 20)
    @Enumerated(EnumType.STRING)
    private MapProvider provider;

    /**
     * Stable unique ID from the map provider.
     * Google Maps: place_id (e.g., "ChIJ0T2NLikpdTERgJJ6o5gX1Kw")
     * Mapbox: mapbox_id (e.g., "dXJuOm1ieGRzdDpmNGI1...")
     * Manual/seed: custom code (e.g., "vn-province-79")
     */
    @Column(name = "provider_id", unique = true, length = 255)
    private String providerId;

    // ==================== BASIC INFO ====================

    /** Primary display name, e.g., "Đại Học Bách Khoa TP.HCM", "Nha Trang". */
    @Column(name = "name", nullable = false, length = 500)
    private String name;

    /**
     * Internationalized/English name for global-scale filtering and search.
     * E.g., "Ho Chi Minh City", "Hanoi".
     */
    @Column(name = "name_en", length = 500)
    private String nameEn;

    /**
     * Full formatted address from Map API.
     * Google Maps: formatted_address (e.g., "268 Đ. Lý Thường Kiệt, Ho Chi Minh City, 72500, Vietnam")
     */
    @Column(name = "full_address", columnDefinition = "TEXT")
    private String fullAddress;

    /**
     * Short place-level formatted summary (city + postcode + country).
     * Used for display in compact UI contexts.
     * Mapbox: place_formatted (e.g., "Quận 10, Hồ Chí Minh, Vietnam")
     */
    @Column(name = "place_formatted", columnDefinition = "TEXT")
    private String placeFormatted;

    // ==================== COORDINATES (PostGIS) ====================

    /**
     * Main location coordinates as PostGIS Point geometry (SRID 4326 = WGS84).
     * Used for all spatial queries (ST_DWithin, ST_Distance, ST_Within, etc.)
     * GiST index on this column enables fast bounding-box and K-NN searches.
     * Google Maps: geometry.location
     */
    @Column(name = "coordinates", columnDefinition = "geometry(Point,4326)")
    private Point coordinates;

    /** Latitude — duplicate for O(1) non-spatial access and B-tree indexing. */
    @Column(name = "latitude", nullable = false)
    private Double latitude;

    /** Longitude — duplicate for O(1) non-spatial access and B-tree indexing. */
    @Column(name = "longitude", nullable = false)
    private Double longitude;

    /**
     * Routable entry-point latitude — where navigation/directions should target.
     * Often differs from the main centroid for large places (airports, universities).
     * Mapbox: routable_points[0].coordinates[1]
     */
    @Column(name = "routable_latitude")
    private Double routableLatitude;

    /** Routable entry-point longitude. Mapbox: routable_points[0].coordinates[0] */
    @Column(name = "routable_longitude")
    private Double routableLongitude;

    // ==================== VIEWPORT / BOUNDING BOX ====================

    /**
     * Viewport bounding box — the recommended map viewport to display this location.
     * Stored as JSONB: {"northeast": {"lat": x, "lng": y}, "southwest": {"lat": x, "lng": y}}
     * Google Maps: geometry.viewport
     * Mapbox: bbox [minLng, minLat, maxLng, maxLat]
     */
    @Column(name = "viewport", columnDefinition = "JSONB")
    @JdbcTypeCode(SqlTypes.JSON)
    private String viewport;

    // ==================== ADDRESS HIERARCHY ====================

    /** Structured address decomposition — mirrors Google Maps address_components. */
    @Embedded
    private AddressComponents addressComponents;

    // ==================== CATEGORIES & UI ====================

    /**
     * POI type categories from Map API.
     * Google Maps: types[] (e.g., ["university", "point_of_interest", "establishment"])
     * Mapbox: categories[] (e.g., ["education", "university"])
     * Stored as JSON array string for flexible multi-value querying.
     */
    @Column(name = "poi_categories", columnDefinition = "TEXT")
    @Convert(converter = StringListConverter.class)
    private List<String> poiCategories;

    /**
     * Primary type representing the most specific category.
     * Google Maps: primary_type (e.g., "university")
     * Useful for icon selection and category filtering.
     */
    @Column(name = "primary_type", length = 100)
    private String primaryType;

    /**
     * Maki icon identifier — Mapbox's open-source icon set.
     * Used by frontend to render map markers and list icons.
     * E.g., "school", "restaurant", "hotel", "museum".
     */
    @Column(name = "maki", length = 50)
    private String maki;

    /**
     * Google Maps icon URL for this place type.
     * Google Maps: icon (e.g., "https://maps.gstatic.com/mapfiles/place_api/icons/...")
     */
    @Column(name = "icon_url", length = 500)
    private String iconUrl;

    /**
     * Background color hex for the icon (Google Maps: icon_background_color).
     * E.g., "#4DB546" for parks, "#FF9E67" for restaurants.
     */
    @Column(name = "icon_background_color", length = 10)
    private String iconBackgroundColor;

    // ==================== RATINGS & REVIEWS ====================

    /**
     * Average user rating (1.0 – 5.0).
     * Google Maps: rating
     */
    @Column(name = "rating", precision = 3, scale = 1)
    private BigDecimal rating;

    /**
     * Total number of user ratings submitted.
     * Google Maps: user_ratings_total
     */
    @Column(name = "user_ratings_total")
    private Integer userRatingsTotal;

    /**
     * Google Maps price level (0=free, 1=$, 2=$$, 3=$$$, 4=$$$$).
     * Google Maps: price_level
     */
    @Column(name = "price_level")
    private Integer priceLevel;

    // ==================== OPERATIONAL INFO ====================

    /**
     * Detailed operational / business status.
     * Google Maps: business_status (OPERATIONAL, CLOSED_TEMPORARILY, CLOSED_PERMANENTLY)
     */
    @Column(name = "operational_status", length = 30)
    @Enumerated(EnumType.STRING)
    private OperationalStatus operationalStatus;

    /** Phone number in international format, e.g., "+84 28 3865 2670". */
    @Column(name = "hotline", length = 50)
    private String hotline;

    /**
     * Official website URL of the place.
     * Google Maps: website
     */
    @Column(name = "website", length = 1000)
    private String website;

    /**
     * Opening hours as JSONB for flexible querying.
     * Google Maps: opening_hours (weekday_text, periods, open_now).
     * Stored raw to avoid complex hour schema.
     */
    @Column(name = "opening_hours", columnDefinition = "JSONB")
    @JdbcTypeCode(SqlTypes.JSON)
    private String openingHours;

    // ==================== ADMINISTRATIVE (Tier 1 specific) ====================

    /**
     * ISO 3166-1 alpha-2 country code for fast country-level filtering.
     * Denormalized here for efficient query: WHERE country_code = 'VN'
     * Mirrors addressComponents.countryCode but indexed independently.
     * Essential for multi-country scalability.
     */
    @Column(name = "country_code", length = 3, insertable = false, updatable = false)
    private String countryCode;

    /**
     * Administrative place code for Tier-1 locations.
     * Vietnam: province code (01=HN, 79=HCM, 48=DN, ...).
     * Global: could be ISO 3166-2 subdivision code (e.g., "VN-HN").
     * Used for stable deduplication of seeded administrative data.
     */
    @Column(name = "admin_code", length = 20)
    private String adminCode;

    /**
     * Timezone identifier (IANA tz database).
     * E.g., "Asia/Ho_Chi_Minh", "Asia/Bangkok", "America/New_York".
     * Important for multi-timezone trip planning.
     */
    @Column(name = "timezone", length = 50)
    private String timezone;

    // ==================== ACCESSIBILITY ====================

    /** Wheelchair/disability accessibility. Google Maps: wheelchair_accessible_entrance. */
    @Column(name = "wheelchair_accessible")
    private Boolean wheelchairAccessible;

    // ==================== FULL-TEXT SEARCH ====================

    /**
     * PostgreSQL tsvector for GIN-indexed full-text search.
     * Auto-populated by DB trigger on INSERT/UPDATE.
     * NOT managed by JPA — read-only from Java perspective.
     * Query: WHERE search_vector @@ plainto_tsquery('simple', :query)
     */
    @Column(name = "search_vector", columnDefinition = "tsvector", insertable = false, updatable = false)
    private String searchVector;

    // ==================== RAW API RESPONSE ====================

    /**
     * Raw JSON response from the map provider — stored as PostgreSQL JSONB.
     * Purpose: enables re-indexing, debugging, and forward-compatibility
     * without re-calling external APIs. Query with JSONB operators if needed.
     * Google Maps: full Place Details response.
     */
    @Column(name = "raw_response", columnDefinition = "JSONB")
    @JdbcTypeCode(SqlTypes.JSON)
    private String rawResponse;

    // ==================== RELATIONSHIPS ====================

    @OneToMany(mappedBy = "location", fetch = FetchType.LAZY)
    @JsonIgnore
    @Builder.Default
    private Set<SuggestLocation> suggestLocations = new HashSet<>();

    // ==================== SOFT DELETE ====================

    @Embedded
    @Builder.Default
    private SoftDeleteInfo softDeleteInfo = new SoftDeleteInfo();
}
