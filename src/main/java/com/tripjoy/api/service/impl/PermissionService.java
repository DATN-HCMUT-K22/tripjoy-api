package com.tripjoy.api.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.tripjoy.api.configuration.redis.RedisCacheConfig;
import com.tripjoy.api.dto.request.PermissionRequest;
import com.tripjoy.api.dto.response.PermissionResponse;
import com.tripjoy.api.entity.Permission;
import com.tripjoy.api.mapper.PermissionMapper;
import com.tripjoy.api.repository.PermissionRepository;
import com.tripjoy.api.service.IPermissionService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PermissionService implements IPermissionService {

    PermissionRepository permissionRepository;
    PermissionMapper mapper;

    /**
     * Creates a new permission and evicts the cached permissions list.
     * Cache {@code permission:all} must be invalidated so the next getAll() reflects
     * the newly added permission.
     */
    @CacheEvict(value = RedisCacheConfig.CACHE_PERMISSION_ALL, allEntries = true)
    public PermissionResponse create(PermissionRequest request) {
        Permission permission = mapper.toPermission(request);
        permission = permissionRepository.save(permission);
        return mapper.toPermissionResponse(permission);
    }

    /**
     * Returns all permissions, cached for 24 hours.
     * Permissions are config data that rarely change in production.
     * Cache is evicted on create/delete.
     */
    @Cacheable(value = RedisCacheConfig.CACHE_PERMISSION_ALL, key = "'all'")
    public List<PermissionResponse> getAll() {
        var permissions = permissionRepository.findAll();
        return permissions.stream().map(mapper::toPermissionResponse).collect(Collectors.toList());
    }

    /**
     * Deletes a permission and evicts the cached permissions list.
     */
    @CacheEvict(value = RedisCacheConfig.CACHE_PERMISSION_ALL, allEntries = true)
    public void delete(String permissionId) {
        permissionRepository.deleteById(permissionId);
    }
}
