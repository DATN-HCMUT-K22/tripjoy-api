package com.tripjoy.api.mapper;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.mapstruct.*;

import com.tripjoy.api.configuration.mapper.BaseMapperConfig;
import com.tripjoy.api.dto.request.LocationCreateRequest;
import com.tripjoy.api.dto.response.location.AddressComponentsDto;
import com.tripjoy.api.dto.response.location.AdministrativeLocationResponse;
import com.tripjoy.api.dto.response.location.LocationResponse;
import com.tripjoy.api.entity.Location;
import com.tripjoy.api.entity.embeddable.AddressComponents;
import com.tripjoy.api.enums.LocationType;
import com.tripjoy.api.enums.MapProvider;
import com.tripjoy.api.enums.OperationalStatus;

@Mapper(config = BaseMapperConfig.class)
public interface LocationMapper {

    // PostGIS factory — SRID 4326 = WGS84 (GPS standard)
    GeometryFactory GEOMETRY_FACTORY = new GeometryFactory(new PrecisionModel(), 4326);

    // ==================== Entity → Full Response ====================

    @Mapping(source = "provider", target = "provider", qualifiedByName = "mapProviderToString")
    @Mapping(source = "operationalStatus", target = "operationalStatus", qualifiedByName = "operationalStatusToString")
    @Mapping(source = "locationType", target = "locationType")
    LocationResponse toResponse(Location location);

    // ==================== Entity → Slim Administrative Response ====================

    @Mapping(source = "locationType", target = "locationType", qualifiedByName = "locationTypeToString")
    AdministrativeLocationResponse toAdministrativeResponse(Location location);

    // ==================== Request → Entity ====================

    @Mapping(source = "latitude", target = "latitude")
    @Mapping(source = "longitude", target = "longitude")
    @Mapping(target = "coordinates", expression = "java(createPoint(request.getLongitude(), request.getLatitude()))")
    @Mapping(source = "provider", target = "provider", qualifiedByName = "stringToMapProvider")
    @Mapping(source = "operationalStatus", target = "operationalStatus", qualifiedByName = "stringToOperationalStatus")
    @Mapping(source = "locationType", target = "locationType", qualifiedByName = "resolveLocationType")
    @Mapping(source = "rawMapResponse", target = "rawResponse", qualifiedByName = "validateJson")
    @Mapping(target = "isVerified", constant = "false")
    @Mapping(target = "usageCount", constant = "0")
    @Mapping(target = "searchVector", ignore = true) // managed by DB trigger
    @Mapping(target = "suggestLocations", ignore = true)
    Location toEntity(LocationCreateRequest request);

    // ==================== Partial Update (PATCH-style) ====================

    /**
     * Merges fields from a create request into an existing entity.
     * Null fields in the request are ignored (do not overwrite existing values).
     * Coordinates and PostGIS point are updated together atomically.
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "coordinates", expression = "java(updatePoint(request, entity))")
    @Mapping(source = "provider", target = "provider", qualifiedByName = "stringToMapProvider")
    @Mapping(source = "operationalStatus", target = "operationalStatus", qualifiedByName = "stringToOperationalStatus")
    @Mapping(source = "rawMapResponse", target = "rawResponse", qualifiedByName = "validateJson")
    @Mapping(target = "isVerified", ignore = true) // only admin can change
    @Mapping(target = "usageCount", ignore = true) // managed by batch job
    @Mapping(target = "searchVector", ignore = true) // managed by DB trigger
    @Mapping(target = "suggestLocations", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    void updateEntityFromRequest(LocationCreateRequest request, @MappingTarget Location entity);

    // ==================== AddressComponents mapping ====================

    AddressComponentsDto toAddressComponentsDto(AddressComponents addressComponents);

    AddressComponents toAddressComponents(AddressComponentsDto dto);

    // ==================== Named converters ====================

    @Named("mapProviderToString")
    default String mapProviderToString(MapProvider provider) {
        return provider != null ? provider.name() : null;
    }

    @Named("stringToMapProvider")
    default MapProvider stringToMapProvider(String provider) {
        if (provider == null || provider.isBlank()) return MapProvider.MANUAL;
        try {
            return MapProvider.valueOf(provider.toUpperCase());
        } catch (IllegalArgumentException e) {
            return MapProvider.MANUAL;
        }
    }

    @Named("operationalStatusToString")
    default String operationalStatusToString(OperationalStatus status) {
        return status != null ? status.name() : null;
    }

    @Named("stringToOperationalStatus")
    default OperationalStatus stringToOperationalStatus(String status) {
        if (status == null || status.isBlank()) return OperationalStatus.UNKNOWN;
        try {
            return OperationalStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            return OperationalStatus.UNKNOWN;
        }
    }

    @Named("locationTypeToString")
    default String locationTypeToString(LocationType type) {
        return type != null ? type.name() : null;
    }

    /**
     * Resolves the LocationType from the request, defaulting to POI if null.
     * This ensures the Two-Tier Model constraint: anything without an explicit type is a POI.
     */
    @Named("resolveLocationType")
    default LocationType resolveLocationType(LocationType type) {
        return type != null ? type : LocationType.POI;
    }

    // ==================== PostGIS Point helpers ====================

    /**
     * Create a PostGIS Point from longitude and latitude.
     * IMPORTANT: PostGIS/JTS Coordinate order is X=longitude, Y=latitude.
     * SRID 4326 = WGS84 coordinate system (standard GPS).
     */
    @Named("createPoint")
    default Point createPoint(Double longitude, Double latitude) {
        if (latitude == null || longitude == null) return null;
        Point point = GEOMETRY_FACTORY.createPoint(new Coordinate(longitude, latitude));
        point.setSRID(4326);
        return point;
    }

    /**
     * Conditionally updates the PostGIS Point during partial update.
     * Only creates a new point if both lat and lng are non-null in the request.
     * Otherwise, preserves the existing entity's coordinates.
     */
    @Named("updatePoint")
    default Point updatePoint(LocationCreateRequest request, Location entity) {
        if (request.getLatitude() != null && request.getLongitude() != null) {
            return createPoint(request.getLongitude(), request.getLatitude());
        }
        return entity.getCoordinates(); // preserve existing
    }

    // ==================== JSON Validation ====================

    /**
     * Validates raw JSON string before storing as JSONB in PostgreSQL.
     * Rejects non-JSON strings (plain values, empty, test placeholders).
     */
    @Named("validateJson")
    default String validateJson(String json) {
        if (json == null || json.isBlank()) return null;
        String trimmed = json.trim();
        if (trimmed.equals("string") || trimmed.equals("null")) return null;
        if (!trimmed.startsWith("{") && !trimmed.startsWith("[")) return null;
        return json;
    }
}
