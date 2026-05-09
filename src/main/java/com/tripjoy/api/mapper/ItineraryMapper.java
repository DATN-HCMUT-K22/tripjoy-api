package com.tripjoy.api.mapper;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import com.tripjoy.api.configuration.mapper.BaseMapperConfig;
import com.tripjoy.api.dto.request.ItineraryRequest;
import com.tripjoy.api.dto.response.ItineraryResponse;
import com.tripjoy.api.entity.Itinerary;
import com.tripjoy.api.entity.Theme;

@Mapper(
        config = BaseMapperConfig.class,
        uses = {UserMapper.class})
public interface ItineraryMapper {

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "group", ignore = true)
    @Mapping(target = "themes", ignore = true)
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
    @Mapping(target = "themes", source = "themes", qualifiedByName = "mapThemesToStrings")
    @Mapping(source = "budgetEstimate", target = "budgetEstimate")
    ItineraryResponse toItineraryResponse(Itinerary itinerary);

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "group", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "themes", ignore = true)
    @Mapping(target = "tripItems", ignore = true)
    @Mapping(target = "expenses", ignore = true)
    @Mapping(target = "travelNotebook", ignore = true)
    @Mapping(target = "favouriteUsers", ignore = true)
    @Mapping(target = "origin", ignore = true)
    @Mapping(target = "destination", ignore = true)
    void updateItinerary(@MappingTarget Itinerary itinerary, ItineraryRequest request);

    @Named("mapThemesToStrings")
    default Set<String> mapThemesToStrings(Set<Theme> themes) {
        if (themes == null) {
            return Collections.emptySet();
        }
        return themes.stream().map(Theme::getName).collect(Collectors.toSet());
    }
}
