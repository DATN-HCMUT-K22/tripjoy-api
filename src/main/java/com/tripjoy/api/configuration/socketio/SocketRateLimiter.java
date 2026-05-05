package com.tripjoy.api.configuration.socketio;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.tripjoy.api.service.ISystemConfigService;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

/**
 * Rate limiter for Socket.IO events to prevent spam and DoS attacks.
 * Uses token bucket algorithm via Bucket4j.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SocketRateLimiter {

    ISystemConfigService configService;

    Map<String, Bucket> messageBuckets = new ConcurrentHashMap<>();
    Map<String, Bucket> typingBuckets = new ConcurrentHashMap<>();

    static int DEFAULT_MESSAGE_LIMIT = 10;
    static int DEFAULT_TYPING_LIMIT = 1;

    /**
     * Check if message event is allowed for user.
     * Limit: 10 messages per second per user.
     */
    public boolean allowMessage(String userId) {
        Bucket bucket = messageBuckets.computeIfAbsent(userId, k -> createMessageBucket());
        boolean allowed = bucket.tryConsume(1);

        if (!allowed) {
            log.warn("Rate limit exceeded for messages: userId={}", userId);
        }

        return allowed;
    }

    /**
     * Check if typing event is allowed for user.
     * Limit: 1 typing event per second per user.
     */
    public boolean allowTyping(String userId) {
        Bucket bucket = typingBuckets.computeIfAbsent(userId, k -> createTypingBucket());
        return bucket.tryConsume(1);
    }

    private Bucket createMessageBucket() {
        int limitValue = configService.getIntValue("CHAT_MSG_RATE_LIMIT", DEFAULT_MESSAGE_LIMIT);
        Bandwidth limit = Bandwidth.classic(limitValue, Refill.intervally(limitValue, Duration.ofSeconds(1)));
        return Bucket.builder().addLimit(limit).build();
    }

    private Bucket createTypingBucket() {
        int limitValue = configService.getIntValue("CHAT_TYPING_RATE_LIMIT", DEFAULT_TYPING_LIMIT);
        Bandwidth limit = Bandwidth.classic(limitValue, Refill.intervally(limitValue, Duration.ofSeconds(1)));
        return Bucket.builder().addLimit(limit).build();
    }

    /**
     * Clean up buckets for disconnected users to free memory.
     */
    public void cleanup(String userId) {
        messageBuckets.remove(userId);
        typingBuckets.remove(userId);
    }
}
