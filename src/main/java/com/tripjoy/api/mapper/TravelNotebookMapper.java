package com.tripjoy.api.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.tripjoy.api.configuration.mapper.BaseMapperConfig;
import com.tripjoy.api.dto.response.TravelNotebookResponse;
import com.tripjoy.api.dto.response.simple.ItinerarySimpleResponse;
import com.tripjoy.api.entity.Itinerary;
import com.tripjoy.api.entity.TravelNotebook;

@Mapper(config = BaseMapperConfig.class)
public interface TravelNotebookMapper {

    @Mapping(source = "itinerary", target = "itinerary")
    TravelNotebookResponse toResponse(TravelNotebook notebook);

    @Mapping(source = "name", target = "name")
    ItinerarySimpleResponse toItinerarySimpleResponse(Itinerary itinerary);
}
