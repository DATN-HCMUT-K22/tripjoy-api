package com.tripjoy.api.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.tripjoy.api.configuration.mapper.BaseMapperConfig;
import com.tripjoy.api.dto.request.ItineraryRequest;
import com.tripjoy.api.dto.response.ItineraryResponse;
import com.tripjoy.api.entity.Itinerary;

@Mapper(config = BaseMapperConfig.class, uses = {UserMapper.class})
public interface ItineraryMapper {

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "group", ignore = true)
    @Mapping(target = "itineraryThemes", ignore = true)
    @Mapping(target = "tripItems", ignore = true)
    @Mapping(target = "expenses", ignore = true)
    @Mapping(target = "travelNotebook", ignore = true)
    @Mapping(target = "favouriteUsers", ignore = true)
    @Mapping(target = "origin", ignore = true)
    @Mapping(target = "destination", ignore = true)
    Itinerary toItinerary(ItineraryRequest request);

    @Mapping(source = "user", target = "createdByUser")
    @Mapping(source = "name", target = "title")
    @Mapping(source = "group.id", target = "groupId")
    ItineraryResponse toItineraryResponse(Itinerary itinerary);

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "group", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "itineraryThemes", ignore = true)
    @Mapping(target = "tripItems", ignore = true)
    @Mapping(target = "expenses", ignore = true)
    @Mapping(target = "travelNotebook", ignore = true)
    @Mapping(target = "favouriteUsers", ignore = true)
    @Mapping(target = "origin", ignore = true)
    @Mapping(target = "destination", ignore = true)
    void updateItinerary(@MappingTarget Itinerary itinerary, ItineraryRequest request);
}
