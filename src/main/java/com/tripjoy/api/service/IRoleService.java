package com.tripjoy.api.service;

import com.tripjoy.api.dto.request.RoleRequest;
import com.tripjoy.api.dto.response.RoleResponse;

import java.util.List;

public interface IRoleService {
    RoleResponse create(RoleRequest request);

    List<RoleResponse> getAll();

    void delete(String roleId);
}
