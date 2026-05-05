package com.tripjoy.api.configuration.socketio;

import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.corundumstudio.socketio.*;
import com.corundumstudio.socketio.AuthorizationResult;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.protocol.JacksonJsonSupport;
import com.corundumstudio.socketio.store.RedissonStoreFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import com.tripjoy.api.configuration.security.JwtUtils;

import org.springframework.context.annotation.Profile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@RequiredArgsConstructor
@Slf4j
@Profile("!test")
public class SocketIOConfig {

    private final RedissonClient redissonClient;
    private final JwtUtils jwtUtils;

    @Value("${socket-server.host}")
    private String host;

    @Value("${socket-server.port}")
    private Integer port;

    @Bean
    public SocketIOServer socketIOServer(ObjectMapper objectMapper) {
        com.corundumstudio.socketio.Configuration config = new com.corundumstudio.socketio.Configuration();
        config.setHostname(host);
        config.setPort(port);
        config.setMaxFramePayloadLength(1024 * 1024);
        config.setMaxHttpContentLength(1024 * 1024);
        config.setPingTimeout(60000);
        config.setPingInterval(25000);

        JacksonJsonSupport jsonSupport = new JacksonJsonSupport(new ParameterNamesModule(), new JavaTimeModule());
        config.setJsonSupport(jsonSupport);
        config.setStoreFactory(new RedissonStoreFactory(redissonClient));
        config.setAuthorizationListener(this::authorizeConnection);
        config.setExceptionListener(new SocketExceptionHandler());

        SocketIOServer server = new SocketIOServer(config);
        log.info("Socket.IO server configured on {}:{}", host, port);
        return server;
    }

    private AuthorizationResult authorizeConnection(HandshakeData data) {
        try {
            String token = data.getSingleUrlParam("token");
            if (token == null || token.trim().isEmpty()) {
                log.warn(
                        "Socket.IO connection without token from {}",
                        data.getAddress().getHostString());
                return AuthorizationResult.FAILED_AUTHORIZATION;
            }

            jwtUtils.verifyToken(token, false);
            return AuthorizationResult.SUCCESSFUL_AUTHORIZATION;

        } catch (Exception e) {
            log.error("Socket.IO authorization failed: {}", e.getMessage());
            return AuthorizationResult.FAILED_AUTHORIZATION;
        }
    }
}
