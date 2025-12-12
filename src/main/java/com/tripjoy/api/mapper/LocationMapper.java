package com.tripjoy.api.mapper;

import com.tripjoy.api.configuration.mapper.BaseMapperConfig;
import com.tripjoy.api.dto.response.location.LocationResponse;
import com.tripjoy.api.entity.Location;
import org.mapstruct.Mapper;

@Mapper(config = BaseMapperConfig.class)
public interface LocationMapper {

    LocationResponse toResponse(Location location);
}
