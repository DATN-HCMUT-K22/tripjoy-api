package com.tripjoy.api.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.tripjoy.api.configuration.mapper.BaseMapperConfig;
import com.tripjoy.api.dto.request.UserCreationRequest;
import com.tripjoy.api.dto.request.UserProfileUpdateRequest;
import com.tripjoy.api.dto.response.UserResponse;
import com.tripjoy.api.dto.response.simple.UserSimpleResponse;
import com.tripjoy.api.entity.User;

@Mapper(config = BaseMapperConfig.class)
public interface UserMapper {
    User toUser(UserCreationRequest request);

    UserResponse toUserResponse(User user);

    UserSimpleResponse toUserSimpleResponse(User user);

    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "username", ignore = true)
    void updateMyProfile(@MappingTarget User user, UserProfileUpdateRequest request);
}
