package com.tripjoy.api.configuration.socketio;

import com.corundumstudio.socketio.SocketIOServer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class SocketServerRunner implements CommandLineRunner {

    private final SocketIOServer server;

    @Override
    public void run(String... args) {
        try {
            server.start();
            log.info("Socket.IO Server started on port " + server.getConfiguration().getPort());
        } catch (Exception e) {
            log.error("Socket.IO Server failed to start", e);
        }
    }

    // Lưu ý: Cần xử lý @PreDestroy để stop server khi tắt ứng dụng
}