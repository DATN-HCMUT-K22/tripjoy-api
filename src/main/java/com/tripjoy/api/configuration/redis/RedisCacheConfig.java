package com.tripjoy.api.configuration.redis;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

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
 * <p><b>Cache Registry:</b>
 * <pre>
 * ┌────────────────────────┬──────┬────────────────────────────────────────────────────┐
 * │ Cache Name             │ TTL  │ Description                                        │
 * ├────────────────────────┼──────┼────────────────────────────────────────────────────┤
 * │ location:id            │ 24h  │ LocationResponse by UUID                           │
 * │ location:provider      │ 24h  │ Location by external provider ID (reserved)        │
 * │ location:admin         │  6h  │ Verified admin locations (PROVINCE, DISTRICT, etc) │
 * │ auth:invalidated-token │ 1h   │ JWT blacklist check (reduces DB query per request) │
 * │ permission:all         │ 24h  │ All permissions (config data, rarely changes)      │
 * │ role:all               │ 24h  │ All roles (config data, rarely changes)            │
 * │ chat:pinned            │  5m  │ Pinned messages per conversation                   │
 * └────────────────────────┴──────┴────────────────────────────────────────────────────┘
 * </pre>
 *
 * <p><b>Key prefix:</b> {@code tripjoy:} is prepended to all cache names to avoid
 * collisions with Socket.IO on Redis DB 1.
 *
 * <p><b>IMPORTANT:</b> All entries MUST have TTL for the volatile-lru eviction policy.
 */
@Configuration
@EnableCaching
public class RedisCacheConfig {

    // ==================== Cache Names ====================

    // Location
    public static final String CACHE_LOCATION_BY_ID       = "location:id";
    public static final String CACHE_LOCATION_BY_PROVIDER = "location:provider";
    public static final String CACHE_LOCATION_ADMIN        = "location:admin";
    /** Autocomplete suggestions: hybrid DB + Google. Short TTL (10min) because POIs change frequently. */
    public static final String CACHE_LOCATION_AUTOCOMPLETE = "location:autocomplete";

    // Auth
    public static final String CACHE_INVALIDATED_TOKEN = "auth:invalidated-token";

    // RBAC config data
    public static final String CACHE_PERMISSION_ALL = "permission:all";
    public static final String CACHE_ROLE_ALL       = "role:all";

    // Chat
    public static final String CACHE_CHAT_PINNED = "chat:pinned";

    // User — split into two separate namespaces
    /** Public profile: visible to any authenticated user, no sensitive fields. TTL 12h. */
    public static final String CACHE_USER_PUBLIC     = "user:public";
    /** Admin view: full profile including sensitive data. ADMIN-only. TTL 12h. */
    public static final String CACHE_USER_ADMIN_VIEW = "user:admin";

    // Group
    public static final String CACHE_GROUP_BY_ID  = "group:id";
    public static final String CACHE_GROUP_MEMBERS = "group:members";

    // ==================== TTL Durations ====================

    private static final Duration TTL_LOCATION_ID           = Duration.ofHours(24);
    private static final Duration TTL_LOCATION_PROVIDER     = Duration.ofHours(24);
    private static final Duration TTL_LOCATION_ADMIN        = Duration.ofHours(6);
    private static final Duration TTL_LOCATION_AUTOCOMPLETE = Duration.ofMinutes(10);
    private static final Duration TTL_INVALIDATED_TOKEN = Duration.ofHours(1); // matches access token expiry
    private static final Duration TTL_PERMISSION_ALL    = Duration.ofHours(24);
    private static final Duration TTL_ROLE_ALL          = Duration.ofHours(24);
    private static final Duration TTL_CHAT_PINNED       = Duration.ofMinutes(5);
    private static final Duration TTL_USER_PUBLIC     = Duration.ofHours(12);
    private static final Duration TTL_USER_ADMIN_VIEW = Duration.ofHours(12);
    private static final Duration TTL_GROUP_BY_ID       = Duration.ofHours(6);
    private static final Duration TTL_GROUP_MEMBERS     = Duration.ofHours(1);
    private static final Duration TTL_DEFAULT           = Duration.ofHours(1);

    // ==================== Key prefix ====================

    private static final String KEY_PREFIX = "tripjoy:";

    // ==================== Bean ====================

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.activateDefaultTyping(
                objectMapper.getPolymorphicTypeValidator(),
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY);

        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer(objectMapper);

        // Default config — fallback for any cache not explicitly configured below
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

        // Location
        cacheConfigs.put(CACHE_LOCATION_BY_ID,
                defaults.entryTtl(TTL_LOCATION_ID));
        cacheConfigs.put(CACHE_LOCATION_BY_PROVIDER,
                defaults.entryTtl(TTL_LOCATION_PROVIDER));
        cacheConfigs.put(CACHE_LOCATION_ADMIN,
                defaults.entryTtl(TTL_LOCATION_ADMIN));
        cacheConfigs.put(CACHE_LOCATION_AUTOCOMPLETE,
                defaults.entryTtl(TTL_LOCATION_AUTOCOMPLETE));

        // Auth — JWT token blacklist
        cacheConfigs.put(CACHE_INVALIDATED_TOKEN,
                defaults.entryTtl(TTL_INVALIDATED_TOKEN));

        // RBAC config data
        cacheConfigs.put(CACHE_PERMISSION_ALL,
                defaults.entryTtl(TTL_PERMISSION_ALL));
        cacheConfigs.put(CACHE_ROLE_ALL,
                defaults.entryTtl(TTL_ROLE_ALL));

        // Chat
        cacheConfigs.put(CACHE_CHAT_PINNED,
                defaults.entryTtl(TTL_CHAT_PINNED));

        // User
        cacheConfigs.put(CACHE_USER_PUBLIC,
                defaults.entryTtl(TTL_USER_PUBLIC));
        cacheConfigs.put(CACHE_USER_ADMIN_VIEW,
                defaults.entryTtl(TTL_USER_ADMIN_VIEW));

        // Group
        cacheConfigs.put(CACHE_GROUP_BY_ID,
                defaults.entryTtl(TTL_GROUP_BY_ID));
        cacheConfigs.put(CACHE_GROUP_MEMBERS,
                defaults.entryTtl(TTL_GROUP_MEMBERS));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaults)
                .withInitialCacheConfigurations(cacheConfigs)
                .build();
    }
}
