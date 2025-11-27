package com.tripjoy.api.mapper;

import com.tripjoy.api.dto.request.UserCreationRequest;
import com.tripjoy.api.dto.request.UserUpdateRequest;
import com.tripjoy.api.dto.response.UserResponse;
import com.tripjoy.api.entity.Users;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {
    Users toUser(UserCreationRequest request);

    @Mapping(source = "id", target = "id")
    UserResponse toUserResponse(Users users);

//    @BeanMapping(ignoreByDefault = true) // Phớt lờ TẤT CẢ các trường
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "password", ignore = true)
    void updateUser(@MappingTarget Users users, UserUpdateRequest request);
}
