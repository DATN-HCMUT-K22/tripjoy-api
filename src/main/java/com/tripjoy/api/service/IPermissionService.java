package com.tripjoy.api.service;

import com.tripjoy.api.dto.request.PermissionRequest;
import com.tripjoy.api.dto.response.PermissionResponse;

import java.util.List;

public interface IPermissionService {
    PermissionResponse create(PermissionRequest request);

    List<PermissionResponse> getAll();

    void delete(String permissionId);
}
