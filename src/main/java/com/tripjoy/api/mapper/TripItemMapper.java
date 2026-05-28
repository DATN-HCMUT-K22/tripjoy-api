package com.tripjoy.api.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import com.tripjoy.api.configuration.mapper.BaseMapperConfig;
import com.tripjoy.api.dto.request.TripItemRequest;
import com.tripjoy.api.dto.response.TripItemResponse;
import com.tripjoy.api.entity.TripItem;

@Mapper(
        config = BaseMapperConfig.class,
        uses = {LocationMapper.class})
public interface TripItemMapper {

    @Mapping(target = "rating", ignore = true)
    @Mapping(target = "review", ignore = true)
    TripItem toTripItem(TripItemRequest request);

    TripItemResponse toTripItemResponse(TripItem tripItem);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "location", ignore = true)
    @Mapping(target = "itinerary", ignore = true)
    @Mapping(target = "rating", ignore = true)
    @Mapping(target = "review", ignore = true)
    void updateTripItem(@MappingTarget TripItem tripItem, TripItemRequest request);
}
