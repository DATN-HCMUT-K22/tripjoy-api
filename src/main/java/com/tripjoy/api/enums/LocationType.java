package com.tripjoy.api.enums;

/**
 * Hierarchical classification of locations in the TripJoy system.
 *
 * <p>This enum enables the "Two-Tier Location Model":
 * <ul>
 *   <li><b>CONTINENT / COUNTRY / REGION / PROVINCE / DISTRICT</b> — Administrative boundaries.
 *       Pre-seeded into DB. Used for Itinerary origin/destination selectors and global-scale filtering.
 *   <li><b>POI</b> — Point of Interest. Lazily loaded from Map API when users search/pick places
 *       to add to trip items or suggest in group planning.
 * </ul>
 *
 * <p>Designed for global scalability: supports continent → country → region → province → district
 * hierarchy, not just Vietnam.
 */
public enum LocationType {

    // ==================== TIER 1: Administrative Boundaries ====================

    /** Continent-level (e.g., "Asia", "Europe"). Rarely used but supports global scale. */
    CONTINENT,

    /** Country-level (e.g., "Vietnam", "Thailand", "Japan"). */
    COUNTRY,

    /**
     * Region/State/Province grouping (e.g., "Southeast Vietnam", "Northern Thailand").
     * Maps to Google Maps' "administrative_area_level_1" in some countries.
     */
    REGION,

    /**
     * Province / City-level (e.g., "Hồ Chí Minh", "Hà Nội", "Đà Nẵng").
     * This is the PRIMARY type used for Itinerary origin/destination selection.
     * In Vietnam: 63 tỉnh/thành phố.
     * Maps to Google Maps' "administrative_area_level_1" for VN,
     * or "locality" for city-states (Singapore, etc.).
     */
    PROVINCE,

    /**
     * District / Sub-city level (e.g., "Quận 1", "Hoàn Kiếm").
     * Maps to Google Maps' "administrative_area_level_2".
     * Optional in Tier 1; used when users want finer location selection.
     */
    DISTRICT,

    // ==================== TIER 2: Points of Interest ====================

    /**
     * Point of Interest — any named place smaller than a district.
     * Examples: cafes, restaurants, museums, hotels, airports, parks.
     * Lazily loaded from Map API (Google Places / Mapbox) and cached in DB.
     * Maps to Google Maps' types: establishment, point_of_interest, etc.
     */
    POI
}
