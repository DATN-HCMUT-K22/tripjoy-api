package com.tripjoy.api.configuration.socketio;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import lombok.extern.slf4j.Slf4j;

/**
 * Rate limiter for Socket.IO events to prevent spam and DoS attacks.
 * Uses token bucket algorithm via Bucket4j.
 */
@Slf4j
@Component
public class SocketRateLimiter {

    private final Map<String, Bucket> messageBuckets = new ConcurrentHashMap<>();
    private final Map<String, Bucket> typingBuckets = new ConcurrentHashMap<>();

    private static final int MESSAGE_LIMIT = 10; // messages per second
    private static final int TYPING_LIMIT = 1; // typing events per second

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
        Bandwidth limit = Bandwidth.classic(MESSAGE_LIMIT, Refill.intervally(MESSAGE_LIMIT, Duration.ofSeconds(1)));
        return Bucket.builder().addLimit(limit).build();
    }

    private Bucket createTypingBucket() {
        Bandwidth limit = Bandwidth.classic(TYPING_LIMIT, Refill.intervally(TYPING_LIMIT, Duration.ofSeconds(1)));
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
