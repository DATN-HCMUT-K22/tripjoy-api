package com.tripjoy.api.entity;

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
import com.tripjoy.api.enums.MapProvider;
import com.tripjoy.api.enums.OperationalStatus;

import lombok.*;

/**
 * Location entity - Stores geographical locations with Map API integration
 * Supports Mapbox and Google Maps data structure
 * Uses PostGIS for spatial queries and indexing
 */
@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "location",
        indexes = {
            @Index(name = "idx_location_provider_id", columnList = "provider_id"),
            @Index(name = "idx_location_coordinates", columnList = "latitude, longitude")
        })
public class Location extends BaseEntity {

    // ==================== PROVIDER INFO ====================

    @Column(name = "provider", length = 20)
    @Enumerated(EnumType.STRING)
    private MapProvider provider;

    @Column(name = "provider_id", unique = true, length = 255)
    private String providerId; // Unique ID from map provider (mapbox_id or place_id)

    // ==================== BASIC INFO ====================

    @Column(name = "name", nullable = false, length = 500)
    private String name; // Location name (e.g., "Đại Học Bách Khoa TP.HCM")

    @Column(name = "full_address", columnDefinition = "TEXT")
    private String fullAddress; // Full formatted address (e.g., "268 Đ. Lý Thường Kiệt, Ho Chi Minh City,
    // 72500, Vietnam")

    @Column(name = "place_formatted", columnDefinition = "TEXT")
    private String placeFormatted; // Place-level formatted address (city, postcode, country)

    // ==================== COORDINATES (PostGIS) ====================

    @Column(name = "coordinates", columnDefinition = "geometry(Point,4326)")
    private Point coordinates; // Main coordinates as PostGIS Point geometry (SRID 4326 = WGS84)

    @Column(name = "latitude", nullable = false)
    private Double latitude; // Latitude (for convenient access, indexed for non-spatial queries)

    @Column(name = "longitude", nullable = false)
    private Double longitude; // Longitude (for convenient access, indexed for non-spatial queries)

    @Column(name = "routable_latitude")
    private Double routableLatitude; // Routable point latitude (navigation/directions entry point)

    @Column(name = "routable_longitude")
    private Double routableLongitude; // Routable point longitude (navigation/directions entry point)

    // ==================== ADDRESS HIERARCHY ====================

    @Embedded
    private AddressComponents addressComponents; // Structured address components (country, city, district, ward,
    // street, etc.)

    // ==================== CATEGORIES & UI ====================
    // POI Categories: Point of Interest types from Map API (e.g., "education",
    // "university", "landmark")
    // Used for filtering and categorization following Mapbox/Google Maps standards

    @Column(name = "poi_categories", columnDefinition = "TEXT")
    @Convert(converter = StringListConverter.class)
    private List<String> poiCategories; // POI categories from map provider (Mapbox/Google Maps)

    // Maki: Mapbox's icon naming system (e.g., "school", "restaurant", "hotel")
    // Frontend uses this to display appropriate icons on maps and lists
    // See: https://github.com/mapbox/maki
    @Column(name = "maki", length = 50)
    private String maki; // Maki icon name for UI display

    // ==================== OPERATIONAL INFO ====================

    @Column(name = "hotline", length = 50)
    private String hotline; // Contact hotline/phone number

    @Column(name = "operational_status", length = 30)
    @Enumerated(EnumType.STRING)
    private OperationalStatus operationalStatus; // Detailed operational status

    // ==================== METADATA ====================

    @Column(name = "wheelchair_accessible")
    private Boolean wheelchairAccessible; // Wheelchair accessibility information

    @Column(name = "raw_response", columnDefinition = "JSONB")
    @JdbcTypeCode(SqlTypes.JSON)
    private String rawResponse; // Raw JSON response from map provider (for backup/debugging)
    // Stored as JSONB in PostgreSQL for queryability

    // ==================== RELATIONSHIPS ====================

    @OneToMany(mappedBy = "location", fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<SuggestLocation> suggestLocations = new HashSet<>(); // Location suggestions from group members
}
