package com.tripjoy.api.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.tripjoy.api.configuration.mapper.BaseMapperConfig;
import com.tripjoy.api.dto.request.TripItemRequest;
import com.tripjoy.api.dto.response.TripItemResponse;
import com.tripjoy.api.entity.TripItem;

@Mapper(config = BaseMapperConfig.class, uses = {LocationMapper.class})
public interface TripItemMapper {

    TripItem toTripItem(TripItemRequest request);

    TripItemResponse toTripItemResponse(TripItem tripItem);

    @Mapping(target = "location", ignore = true)
    @Mapping(target = "itinerary", ignore = true)
    void updateTripItem(@MappingTarget TripItem tripItem, TripItemRequest request);
}
