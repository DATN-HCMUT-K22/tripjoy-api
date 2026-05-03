package com.tripjoy.api.service;

import java.util.List;
import java.util.Map;

import com.tripjoy.api.dto.request.SystemConfigUpdateRequest;
import com.tripjoy.api.dto.response.SystemConfigResponse;

public interface ISystemConfigService {
    String getValue(String key, String defaultValue);
    int getIntValue(String key, int defaultValue);
    boolean getBooleanValue(String key, boolean defaultValue);
    
    List<SystemConfigResponse> getAllConfigs();
    List<SystemConfigResponse> getConfigsByGroup(String group);
    
    SystemConfigResponse updateConfig(String key, SystemConfigUpdateRequest request);
    
    /**
     * Get all configs as a map for internal use.
     */
    Map<String, String> getConfigMap();
}
