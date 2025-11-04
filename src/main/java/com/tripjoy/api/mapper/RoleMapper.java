package com.tripjoy.api.mapper;

import com.tripjoy.api.dto.request.PermissionRequest;
import com.tripjoy.api.dto.request.RoleRequest;
import com.tripjoy.api.dto.response.PermissionResponse;
import com.tripjoy.api.dto.response.RoleResponse;
import com.tripjoy.api.entity.Permission;
import com.tripjoy.api.entity.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RoleMapper {
    @Mapping(target = "permissions", ignore = true)
    Role toRole(RoleRequest request);

    RoleResponse toRoleResponse(Role role);

}
