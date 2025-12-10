package com.tripjoy.api.mapper;

import com.tripjoy.api.configuration.mapper.BaseMapperConfig;
import com.tripjoy.api.dto.request.PermissionRequest;
import com.tripjoy.api.dto.response.PermissionResponse;
import com.tripjoy.api.entity.Permission;
import org.mapstruct.Mapper;

@Mapper(config = BaseMapperConfig.class)
public interface PermissionMapper {
    Permission toPermission(PermissionRequest request);
    PermissionResponse toPermissionResponse(Permission permission);

}
