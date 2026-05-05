package com.tripjoy.api.configuration.redis;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.context.annotation.Profile;

/**
 * Redisson Configuration for Socket.IO
 * Uses Database 0 to separate from Spring Cache (Database 1)
 * This prevents cache evictions from affecting real-time Socket.IO data
 */
@Configuration
@Profile("!test")
public class RedissonConfig {

    @Value("${spring.data.redis.host:localhost}")
    private String host;

    @Value("${spring.data.redis.port:6379}")
    private int port;

    @Value("${spring.data.redis.password}")
    private String password;

    // Socket.IO uses Database 0 (separate from Cache DB 1)
    private static final int SOCKET_IO_DB_INDEX = 0;

    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        String redisAddress = String.format("redis://%s:%d", host, port);

        // Configure Single Server with Database 0
        SingleServerConfig singleServerConfig = config.useSingleServer()
                .setAddress(redisAddress)
                .setDatabase(SOCKET_IO_DB_INDEX); // Explicitly use DB 0 for Socket.IO

        if (password != null && !password.isBlank()) {
            singleServerConfig.setPassword(password);
        }

        return Redisson.create(config);
    }
}
