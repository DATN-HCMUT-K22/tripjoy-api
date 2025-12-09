package com.tripjoy.api.service.impl;

import com.tripjoy.api.dto.request.RoleRequest;
import com.tripjoy.api.dto.response.RoleResponse;
import com.tripjoy.api.entity.Permission;
import com.tripjoy.api.entity.Role;
import com.tripjoy.api.mapper.RoleMapper;
import com.tripjoy.api.repository.PermissionRepository;
import com.tripjoy.api.repository.RoleRepository;
import com.tripjoy.api.service.IRoleService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RoleService implements IRoleService {
    RoleRepository roleRepository;
    PermissionRepository permissionRepository;
    RoleMapper mapper;

    public RoleResponse create(RoleRequest request) {
        Role role = mapper.toRole(request);

        List<Permission> permissions = permissionRepository.findAllById(request.getPermissions());
        role.setPermissions(new HashSet<>(permissions));

        role = roleRepository.save(role);
        return mapper.toRoleResponse(role);
    }

    public List<RoleResponse> getAll() {
        var roles = roleRepository.findAll();
        return roles.stream()
                .map(mapper::toRoleResponse)
                .toList();
    }

    public void delete(String roleId) {
        roleRepository.deleteById(roleId);
    }
}
