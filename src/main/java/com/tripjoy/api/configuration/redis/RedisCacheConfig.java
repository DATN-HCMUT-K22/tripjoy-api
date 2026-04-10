package com.tripjoy.api.configuration.redis;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis Cache Configuration for Spring @Cacheable / @CacheEvict.
 *
 * <p>Uses Database 1 (configured in application.yaml).
 * Redisson (Socket.IO) uses Database 0 — databases are intentionally separated.
 *
 * <p><b>Cache Names & TTL Strategy:</b>
 * <ul>
 *   <li>{@code location:id}       — 24 h  — POI / Location by UUID. Stable once created.</li>
 *   <li>{@code location:provider} — 24 h  — Location by external provider ID (Google/Mapbox). Stable.</li>
 *   <li>{@code location:admin}    — 6 h   — Verified administrative locations (PROVINCE, DISTRICT).
 *                                            Rarely updated but refreshed more often for populated sort order.</li>
 * </ul>
 *
 * <p><b>Key prefix:</b> {@code tripjoy:} is prepended to all cache names to avoid
 * collisions with any future caches on the same Redis DB 1.
 *
 * <p><b>IMPORTANT:</b> All entries MUST have TTL configured for the volatile-lru eviction policy.
 * Never use {@link RedisCacheConfiguration#defaultCacheConfig()} without an explicit TTL.
 */
@Configuration
@EnableCaching
public class RedisCacheConfig {

    // ==================== Cache Names (use as constants in @Cacheable) ====================

    public static final String CACHE_LOCATION_BY_ID       = "location:id";
    public static final String CACHE_LOCATION_BY_PROVIDER = "location:provider";
    public static final String CACHE_LOCATION_ADMIN        = "location:admin";

    // ==================== TTL Durations ====================

    private static final Duration TTL_LOCATION_ID       = Duration.ofHours(24);
    private static final Duration TTL_LOCATION_PROVIDER = Duration.ofHours(24);
    private static final Duration TTL_LOCATION_ADMIN    = Duration.ofHours(6);
    private static final Duration TTL_DEFAULT           = Duration.ofHours(1);

    // ==================== Key prefix ====================

    private static final String KEY_PREFIX = "tripjoy:";

    // ==================== Bean ====================

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer();

        // Default config — used for any cache NOT listed in withInitialCacheConfigurations()
        RedisCacheConfiguration defaults = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(TTL_DEFAULT)
                .disableCachingNullValues()
                .prefixCacheNameWith(KEY_PREFIX)
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(jsonSerializer));

        // Per-cache TTL overrides
        Map<String, RedisCacheConfiguration> cacheConfigs = new HashMap<>();
        cacheConfigs.put(CACHE_LOCATION_BY_ID,
                defaults.entryTtl(TTL_LOCATION_ID));
        cacheConfigs.put(CACHE_LOCATION_BY_PROVIDER,
                defaults.entryTtl(TTL_LOCATION_PROVIDER));
        cacheConfigs.put(CACHE_LOCATION_ADMIN,
                defaults.entryTtl(TTL_LOCATION_ADMIN));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaults)
                .withInitialCacheConfigurations(cacheConfigs)
                .build();
    }
}
