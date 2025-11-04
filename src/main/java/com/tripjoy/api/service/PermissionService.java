package com.tripjoy.api.service;

import com.tripjoy.api.dto.request.PermissionRequest;
import com.tripjoy.api.dto.response.PermissionResponse;
import com.tripjoy.api.entity.Permission;
import com.tripjoy.api.mapper.PermissionMapper;
import com.tripjoy.api.repository.PermissionRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PermissionService {
    PermissionRepository permissionRepository;
    PermissionMapper mapper;

    public PermissionResponse create(PermissionRequest request) {
        Permission permission = mapper.toPermission(request);
        permission = permissionRepository.save(permission);
        return mapper.toPermissionResponse(permission);
    }

    public List<PermissionResponse> getAll() {
        var permissions = permissionRepository.findAll();

        return permissions.stream()
                .map(mapper::toPermissionResponse)
                .toList();
    }

    public void delete(String permissionId) {
        permissionRepository.deleteById(permissionId);
    }
}
