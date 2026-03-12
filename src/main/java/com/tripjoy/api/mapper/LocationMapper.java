package com.tripjoy.api.mapper;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import com.tripjoy.api.configuration.mapper.BaseMapperConfig;
import com.tripjoy.api.dto.request.LocationCreateRequest;
import com.tripjoy.api.dto.response.location.AddressComponentsDto;
import com.tripjoy.api.dto.response.location.LocationResponse;
import com.tripjoy.api.entity.Location;
import com.tripjoy.api.entity.embeddable.AddressComponents;
import com.tripjoy.api.enums.MapProvider;
import com.tripjoy.api.enums.OperationalStatus;

@Mapper(config = BaseMapperConfig.class)
public interface LocationMapper {

    // GeometryFactory with SRID 4326 (WGS84 - GPS standard)
    GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    @Mapping(source = "latitude", target = "latitude")
    @Mapping(source = "longitude", target = "longitude")
    @Mapping(source = "provider", target = "provider", qualifiedByName = "mapProviderToString")
    @Mapping(source = "operationalStatus", target = "operationalStatus", qualifiedByName = "operationalStatusToString")
    LocationResponse toResponse(Location location);

    @Mapping(source = "latitude", target = "latitude")
    @Mapping(source = "longitude", target = "longitude")
    @Mapping(target = "coordinates", expression = "java(createPoint(request.getLongitude(), request.getLatitude()))")
    @Mapping(source = "provider", target = "provider", qualifiedByName = "stringToMapProvider")
    @Mapping(source = "operationalStatus", target = "operationalStatus", qualifiedByName = "stringToOperationalStatus")
    @Mapping(source = "rawMapResponse", target = "rawResponse", qualifiedByName = "validateJson")
    Location toEntity(LocationCreateRequest request);

    AddressComponentsDto toAddressComponentsDto(AddressComponents addressComponents);

    AddressComponents toAddressComponents(AddressComponentsDto dto);

    @Named("mapProviderToString")
    default String mapProviderToString(MapProvider provider) {
        return provider != null ? provider.name() : null;
    }

    @Named("stringToMapProvider")
    default MapProvider stringToMapProvider(String provider) {
        return provider != null ? MapProvider.valueOf(provider.toUpperCase()) : MapProvider.MANUAL;
    }

    @Named("operationalStatusToString")
    default String operationalStatusToString(OperationalStatus status) {
        return status != null ? status.name() : null;
    }

    @Named("stringToOperationalStatus")
    default OperationalStatus stringToOperationalStatus(String status) {
        return status != null ? OperationalStatus.valueOf(status.toUpperCase()) : OperationalStatus.UNKNOWN;
    }

    // ==================== PostGIS Point Mapping ====================

    /**
     * Create PostGIS Point from longitude and latitude
     * Note: PostGIS uses (longitude, latitude) order, not (lat, lng)
     * SRID 4326 = WGS84 coordinate system (standard GPS)
     */
    @Named("createPoint")
    default Point createPoint(Double longitude, Double latitude) {
        if (latitude == null || longitude == null) {
            return null;
        }
        // IMPORTANT: Coordinate order is (X=longitude, Y=latitude)
        return geometryFactory.createPoint(new Coordinate(longitude, latitude));
    }

    /**
     * Extract latitude from PostGIS Point
     */
    @Named("extractLatitude")
    default Double extractLatitude(Point point) {
        return point != null ? point.getY() : null;
    }

    /**
     * Extract longitude from PostGIS Point
     */
    @Named("extractLongitude")
    default Double extractLongitude(Point point) {
        return point != null ? point.getX() : null;
    }

    // ==================== JSON Validation ====================

    /**
     * Validate JSON string - return null if invalid or just literal "string"
     * This prevents PostgreSQL JSONB errors from test data
     */
    @Named("validateJson")
    default String validateJson(String json) {
        if (json == null || json.trim().isEmpty() || json.trim().equals("string")) {
            return null;
        }

        // Check if it's valid JSON (starts with { or [)
        String trimmed = json.trim();
        if (!trimmed.startsWith("{") && !trimmed.startsWith("[")) {
            return null;
        }

        return json;
    }
}
