package com.tripjoy.api.configuration.socketio;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.listener.DefaultExceptionListener;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * Custom exception handler for Socket.IO events.
 * Provides centralized error handling, logging, and client error notification.
 */
@Slf4j
public class SocketExceptionHandler extends DefaultExceptionListener {

    @Override
    public void onEventException(Exception e, List<Object> args, SocketIOClient client) {
        String userId = client != null ? client.getHandshakeData().getSingleUrlParam("userId") : "unknown";
        String sessionId = client != null ? client.getSessionId().toString() : "unknown";

        log.error("Socket.IO event exception: userId={}, sessionId={}, args={}, error={}",
                userId, sessionId, args, e.getMessage(), e);

        if (client != null) {
            client.sendEvent("error", createErrorResponse("Event processing failed", e.getMessage()));
        }
    }

    @Override
    public void onDisconnectException(Exception e, SocketIOClient client) {
        String userId = client != null ? client.getHandshakeData().getSingleUrlParam("userId") : "unknown";
        log.error("Socket.IO disconnect exception: userId={}, error={}", userId, e.getMessage());
    }

    @Override
    public void onConnectException(Exception e, SocketIOClient client) {
        log.error("Socket.IO connect exception: error={}", e.getMessage());

        if (client != null) {
            client.sendEvent("error", createErrorResponse("Connection failed", e.getMessage()));
        }
    }

    @Override
    public boolean exceptionCaught(ChannelHandlerContext ctx, Throwable e) {
        log.error("Socket.IO channel exception: {}", e.getMessage(), e);
        return true; // Continue processing
    }

    private ErrorResponse createErrorResponse(String type, String message) {
        return new ErrorResponse(type, message);
    }

    /**
     * Simple error response DTO for client notification.
     */
    public static class ErrorResponse {
        public String type;
        public String message;
        public long timestamp;

        public ErrorResponse(String type, String message) {
            this.type = type;
            this.message = message;
            this.timestamp = System.currentTimeMillis();
        }
    }
}
