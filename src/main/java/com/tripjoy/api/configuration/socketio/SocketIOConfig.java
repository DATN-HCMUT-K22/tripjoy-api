package com.tripjoy.api.configuration.socketio;

import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.store.RedissonStoreFactory;
import com.tripjoy.api.configuration.security.JwtUtils;
import com.tripjoy.api.dto.response.auth.AuthenticationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.corundumstudio.socketio.AuthorizationResult;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class SocketIOConfig {

    private final RedissonClient redissonClient; // Inject Redis client đã tạo ở bước 2
    private final JwtUtils jwtUtils;

    @Value("${socket-server.host}")
    private String host;

    @Value("${socket-server.port}")
    private Integer port;

    @Bean
    public SocketIOServer socketIOServer() {
        com.corundumstudio.socketio.Configuration config = new com.corundumstudio.socketio.Configuration();
        config.setHostname(host);
        config.setPort(port);

        // --- CẤU HÌNH STORE FACTORY (QUAN TRỌNG) ---
        // Dòng này bảo Socket.IO: "Đừng lưu client trong RAM, hãy lưu vào Redis qua Redisson"
        // Điều này cho phép Pub/Sub hoạt động giữa nhiều server backend.
        config.setStoreFactory(new RedissonStoreFactory(redissonClient));

        // --- Cấu hình Authentication ---
        config.setAuthorizationListener(data -> {
            // Client gửi: ws://host:8085?token=eyJ...
            String token = data.getSingleUrlParam("token");

            if (token == null || token.isEmpty()) {
                log.error("SocketIO: Token missing");
                return AuthorizationResult.FAILED_AUTHORIZATION;
            }

            try {
                // Sử dụng JwtProvider để validate
                // Hàm verifyToken sẽ throw Exception nếu token sai/hết hạn
                jwtUtils.verifyToken(token, false);

                // (Tuỳ chọn) Lấy userId từ token để dùng sau này nếu cần
                // String userId = jwtProvider.getUserIdFromToken(token);

                return AuthorizationResult.SUCCESSFUL_AUTHORIZATION; // Token hợp lệ -> Cho phép kết nối
            } catch (Exception e) {
                log.error("SocketIO: Token invalid - {}", e.getMessage());
                return AuthorizationResult.FAILED_AUTHORIZATION; // Từ chối kết nối
            }
        });

        return new SocketIOServer(config);
    }
}