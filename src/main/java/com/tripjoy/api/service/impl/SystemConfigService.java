package com.tripjoy.api.service.impl;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tripjoy.api.configuration.redis.RedisCacheConfig;
import com.tripjoy.api.dto.request.SystemConfigUpdateRequest;
import com.tripjoy.api.dto.response.SystemConfigResponse;
import com.tripjoy.api.entity.SystemConfig;
import com.tripjoy.api.exception.AppException;
import com.tripjoy.api.exception.ErrorCode;
import com.tripjoy.api.repository.SystemConfigRepository;
import com.tripjoy.api.service.ISystemConfigService;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SystemConfigService implements ISystemConfigService {

    SystemConfigRepository systemConfigRepository;
    CircuitBreakerRegistry circuitBreakerRegistry;
    RetryRegistry retryRegistry;

    private static final String AI_SERVICE_NAME = "aiService";
    @Cacheable(value = RedisCacheConfig.CACHE_SYSTEM_CONFIG, key = "#key")
    public String getValue(String key, String defaultValue) {
        return systemConfigRepository.findById(key)
                .map(SystemConfig::getConfigValue)
                .orElse(defaultValue);
    }

    @Override
    @Cacheable(value = RedisCacheConfig.CACHE_SYSTEM_CONFIG, key = "#key")
    public int getIntValue(String key, int defaultValue) {
        String value = getValue(key, String.valueOf(defaultValue));
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            log.error("Failed to parse system config {} as int: {}", key, value);
            return defaultValue;
        }
    }

    @Override
    @Cacheable(value = RedisCacheConfig.CACHE_SYSTEM_CONFIG, key = "#key")
    public boolean getBooleanValue(String key, boolean defaultValue) {
        String value = getValue(key, String.valueOf(defaultValue));
        return Boolean.parseBoolean(value);
    }

    @Override
    public List<SystemConfigResponse> getAllConfigs() {
        return systemConfigRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<SystemConfigResponse> getConfigsByGroup(String group) {
        return systemConfigRepository.findByConfigGroup(group).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    @CacheEvict(value = RedisCacheConfig.CACHE_SYSTEM_CONFIG, key = "#key")
    public SystemConfigResponse updateConfig(String key, SystemConfigUpdateRequest request) {
        SystemConfig config = systemConfigRepository.findById(key)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Config key not found: " + key));

        config.setConfigValue(request.getValue());
        if (request.getDescription() != null) {
            config.setDescription(request.getDescription());
        }

        config = systemConfigRepository.save(config);
        log.info("System config updated: {} = {}", key, request.getValue());
        
        // Trigger resilience refresh if needed
        if (key.startsWith("AI_CB_") || key.startsWith("AI_RETRY_")) {
            refreshResilienceConfigs();
        }

        return mapToResponse(config);
    }

    private void refreshResilienceConfigs() {
        log.info("Refreshing Resilience4j configs for AI Service...");
        
        // 1. Update Circuit Breaker
        circuitBreakerRegistry.find(AI_SERVICE_NAME).ifPresent(cb -> {
            CircuitBreakerConfig currentConfig = cb.getCircuitBreakerConfig();
            CircuitBreakerConfig newConfig = CircuitBreakerConfig.from(currentConfig)
                .slidingWindowSize(getIntValue("AI_CB_SLIDING_WINDOW_SIZE", 5))
                .minimumNumberOfCalls(getIntValue("AI_CB_MIN_CALLS", 3))
                .failureRateThreshold(getIntValue("AI_CB_FAILURE_RATE_THRESHOLD", 50))
                .build();
            
            // Re-register to apply changes
            circuitBreakerRegistry.remove(AI_SERVICE_NAME);
            circuitBreakerRegistry.circuitBreaker(AI_SERVICE_NAME, newConfig);
            log.info("Updated Circuit Breaker config for {}", AI_SERVICE_NAME);
        });

        // 2. Update Retry
        retryRegistry.find(AI_SERVICE_NAME).ifPresent(r -> {
            RetryConfig currentConfig = r.getRetryConfig();
            RetryConfig newConfig = RetryConfig.from(currentConfig)
                .maxAttempts(getIntValue("AI_RETRY_MAX_ATTEMPTS", 3))
                .waitDuration(java.time.Duration.ofSeconds(getIntValue("AI_RETRY_WAIT_DURATION", 2)))
                .build();
            
            retryRegistry.remove(AI_SERVICE_NAME);
            retryRegistry.retry(AI_SERVICE_NAME, newConfig);
            log.info("Updated Retry config for {}", AI_SERVICE_NAME);
        });
    }

    @Override
    public Map<String, String> getConfigMap() {
        return systemConfigRepository.findAll().stream()
                .collect(Collectors.toMap(SystemConfig::getConfigKey, SystemConfig::getConfigValue));
    }

    private SystemConfigResponse mapToResponse(SystemConfig config) {
        return SystemConfigResponse.builder()
                .key(config.getConfigKey())
                .value(config.getConfigValue())
                .dataType(config.getDataType())
                .group(config.getConfigGroup())
                .description(config.getDescription())
                .updatedAt(config.getUpdatedAt())
                .updatedBy(config.getUpdatedBy())
                .build();
    }
}
