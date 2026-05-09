package com.tripjoy.api.configuration.socketio;

import jakarta.annotation.PostConstruct;

import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.corundumstudio.socketio.SocketIOServer;
import com.tripjoy.api.service.impl.SocketService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Order(1)
@RequiredArgsConstructor
@Slf4j
@Profile("!test")
public class SocketIOListenerInitializer {

    private final SocketIOServer server;
    private final SocketService socketService;

    @PostConstruct
    public void registerEventListeners() {
        server.addListeners(socketService);
        log.info("Socket.IO event listeners registered");
    }
}
