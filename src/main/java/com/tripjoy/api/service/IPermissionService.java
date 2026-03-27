package com.tripjoy.api.service;

import java.util.List;

import com.tripjoy.api.dto.request.PermissionRequest;
import com.tripjoy.api.dto.response.PermissionResponse;

public interface IPermissionService {
    PermissionResponse create(PermissionRequest request);

    List<PermissionResponse> getAll();

    void delete(String permissionId);
}
