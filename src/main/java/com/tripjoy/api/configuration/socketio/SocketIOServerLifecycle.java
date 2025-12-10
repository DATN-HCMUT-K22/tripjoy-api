package com.tripjoy.api.configuration.socketio;

import com.corundumstudio.socketio.SocketIOServer;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SocketIOServerLifecycle implements CommandLineRunner {

    private final SocketIOServer server;

    @Override
    public void run(String... args) {
        server.start();
        log.info("Socket.IO server started successfully");
    }

    @PreDestroy
    public void stopServer() {
        server.stop();
        log.info("Socket.IO server stopped");
    }
}
