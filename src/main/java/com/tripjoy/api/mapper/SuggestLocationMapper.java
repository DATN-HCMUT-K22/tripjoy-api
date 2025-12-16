package com.tripjoy.api.mapper;

import com.tripjoy.api.configuration.mapper.BaseMapperConfig;
import com.tripjoy.api.dto.response.SuggestLocationResponse;
import com.tripjoy.api.entity.SuggestLocation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(config = BaseMapperConfig.class, uses = { LocationMapper.class, UserMapper.class })
public interface SuggestLocationMapper {

    @Mapping(source = "group.id", target = "groupId")
    @Mapping(source = "suggestedBy", target = "suggestedBy")
    @Mapping(source = "location", target = "location")
    SuggestLocationResponse toResponse(SuggestLocation entity);

    List<SuggestLocationResponse> toResponseList(List<SuggestLocation> entities);
}
