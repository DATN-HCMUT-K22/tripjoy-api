package com.tripjoy.api.mapper;

import com.tripjoy.api.configuration.mapper.BaseMapperConfig;
import com.tripjoy.api.dto.response.NotificationResponse;
import com.tripjoy.api.entity.Notification;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = BaseMapperConfig.class, uses = { UserMapper.class })
public interface NotificationMapper {

    /**
     * Entity -> Response
     */
    @Mapping(source = "recipient", target = "recipient")
    @Mapping(source = "actor", target = "actor")
    NotificationResponse toResponse(Notification notification);
}
