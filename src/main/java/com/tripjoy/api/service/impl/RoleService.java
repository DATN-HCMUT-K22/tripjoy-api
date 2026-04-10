package com.tripjoy.api.service.impl;

import java.util.HashSet;
import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.tripjoy.api.configuration.redis.RedisCacheConfig;
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

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RoleService implements IRoleService {

    RoleRepository roleRepository;
    PermissionRepository permissionRepository;
    RoleMapper mapper;

    /**
     * Creates a new role and evicts the cached role list.
     */
    @CacheEvict(value = RedisCacheConfig.CACHE_ROLE_ALL, allEntries = true)
    public RoleResponse create(RoleRequest request) {
        Role role = mapper.toRole(request);

        List<Permission> permissions = permissionRepository.findAllById(request.getPermissions());
        role.setPermissions(new HashSet<>(permissions));

        role = roleRepository.save(role);
        return mapper.toRoleResponse(role);
    }

    /**
     * Returns all roles, cached for 24 hours.
     * Roles (USER, ADMIN, MODERATOR) are config data that rarely change.
     * The scope/authority check at auth time calls this indirectly through the JWT
     * scope builder — caching here significantly reduces DB roundtrips.
     */
    @Cacheable(value = RedisCacheConfig.CACHE_ROLE_ALL, key = "'all'")
    public List<RoleResponse> getAll() {
        var roles = roleRepository.findAll();
        return roles.stream().map(mapper::toRoleResponse).toList();
    }

    /**
     * Deletes a role and evicts the cached role list.
     */
    @CacheEvict(value = RedisCacheConfig.CACHE_ROLE_ALL, allEntries = true)
    public void delete(String roleId) {
        roleRepository.deleteById(roleId);
    }
}
