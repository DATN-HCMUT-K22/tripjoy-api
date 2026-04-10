package com.tripjoy.api.configuration.security;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.StringJoiner;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.tripjoy.api.configuration.redis.RedisCacheConfig;
import com.tripjoy.api.entity.User;
import com.tripjoy.api.exception.AppException;
import com.tripjoy.api.exception.ErrorCode;
import com.tripjoy.api.repository.InvalidatedTokenRepository;

import lombok.RequiredArgsConstructor;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;

/**
 * JWT utility for generating, verifying, and blacklisting tokens.
 *
 * <h3>Token Blacklist — Cache Strategy</h3>
 * <p>Every API request triggers {@link #verifyToken} which calls
 * {@code invalidatedTokenRepository.existsById(tokenId)} — a SELECT against the DB.
 * At 100 req/s this is 100 DB queries/s just for blacklist checking.
 *
 * <p>Solution: <b>Redis cache ({@code auth:invalidated-token})</b>
 * <ul>
 *   <li>Key: {@code {jti}} — the JWT ID (UUID)</li>
 *   <li>Value: {@code "1"} (presence = invalidated)</li>
 *   <li>TTL: 1 hour (matches access token expiry — after expiry the token is invalid
 *       regardless of blacklist, so keeping it in cache longer wastes memory)</li>
 * </ul>
 *
 * <p>On {@code existsInBlacklist(jti)}:
 * <ol>
 *   <li>Cache HIT → token is invalidated (return true, no DB call)</li>
 *   <li>Cache MISS → query DB; if found, populate cache; return result</li>
 * </ol>
 *
 * <p>On {@code addToBlacklist(jti)} (called from logout / refresh):
 * <ol>
 *   <li>Save to DB ({@code InvalidatedToken})</li>
 *   <li>Immediately populate the Redis cache (via {@link #addToBlacklist}) so the very
 *       next request for the same token hits cache, not DB</li>
 * </ol>
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class JwtUtils {

    private final InvalidatedTokenRepository invalidatedTokenRepository;

    @NonFinal
    @Value("${jwt.signer-key}")
    protected String SIGNER_KEY;

    @NonFinal
    @Value("${jwt.expiration}")
    protected long EXPIRATION;

    @NonFinal
    @Value("${jwt.refresh-expiration}")
    protected long REFRESH_EXPIRATION;

    // --- 1. Tạo Access Token ---
    public String generateToken(User user) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getId().toString())
                .issuer("tripjoy")
                .issueTime(new Date())
                .expirationTime(new Date(
                        Instant.now().plus(EXPIRATION, ChronoUnit.SECONDS).toEpochMilli()))
                .jwtID(UUID.randomUUID().toString())
                .claim("scope", buildScope(user))
                .claim("userId", user.getId().toString())
                .claim("type", "access")
                .build();

        Payload payload = new Payload(jwtClaimsSet.toJSONObject());
        JWSObject jwsObject = new JWSObject(header, payload);

        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            log.error("Cannot create token", e);
            throw new RuntimeException(e);
        }
    }

    // --- 2. Tạo Refresh Token ---
    public String generateRefreshToken(User user) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getId().toString())
                .issuer("tripjoy")
                .issueTime(new Date())
                .expirationTime(new Date(Instant.now()
                        .plus(REFRESH_EXPIRATION, ChronoUnit.SECONDS)
                        .toEpochMilli()))
                .jwtID(UUID.randomUUID().toString())
                .claim("type", "refresh")
                .build();

        Payload payload = new Payload(jwtClaimsSet.toJSONObject());
        JWSObject jwsObject = new JWSObject(header, payload);

        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            log.error("Cannot create refresh token", e);
            throw new RuntimeException(e);
        }
    }

    // --- 3. Xác thực Token ---
    public SignedJWT verifyToken(String token, boolean isRefresh) throws JOSEException, ParseException {
        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());
        SignedJWT signedJWT = SignedJWT.parse(token);

        Date expiresAt = isRefresh
                ? new Date(signedJWT
                        .getJWTClaimsSet()
                        .getIssueTime()
                        .toInstant()
                        .plus(REFRESH_EXPIRATION, ChronoUnit.SECONDS)
                        .toEpochMilli())
                : signedJWT.getJWTClaimsSet().getExpirationTime();

        var isVerified = signedJWT.verify(verifier);

        // Kiểm tra chữ ký và hạn
        if (!(isVerified && expiresAt.after(new Date()))) throw new AppException(ErrorCode.UNAUTHENTICATED);

        // Kiểm tra blacklist — Redis cache giảm DB query per request
        UUID tokenId = UUID.fromString(signedJWT.getJWTClaimsSet().getJWTID());
        if (existsInBlacklist(tokenId.toString())) throw new AppException(ErrorCode.UNAUTHENTICATED);

        return signedJWT;
    }

    /**
     * Check if a JTI (JWT ID) is in the token blacklist.
     *
     * <p>Backed by Redis cache {@code auth:invalidated-token}.
     * On cache MISS: queries DB and caches the result (Boolean).
     * On cache HIT: skips DB entirely — O(1) Redis GET.
     *
     * <p>Cache key is the JTI string. The ABSENCE of a key means the token is valid.
     * {@code @Cacheable} caches the return value of the DB call —
     * if DB returns {@code false} (not blacklisted), nothing is cached (due to
     * {@code disableCachingNullValues}). If DB returns {@code true}, we cache it.
     *
     * <p>NOTE: We cannot use {@code disableCachingNullValues} to prevent caching
     * {@code false}, so instead we return a sentinel string {@code "blacklisted"}
     * and interpret the absence of cache key as "valid".
     *
     * @param jti JWT ID as String
     * @return true if the token has been invalidated (logged out)
     */
    @Cacheable(
        value = RedisCacheConfig.CACHE_INVALIDATED_TOKEN,
        key = "#jti",
        condition = "#jti != null",
        unless = "#result == false"   // do NOT cache 'false' — only cache invalidated tokens
    )
    public boolean existsInBlacklist(String jti) {
        log.debug("Cache MISS — checking token blacklist in DB: jti={}", jti);
        return invalidatedTokenRepository.existsById(UUID.fromString(jti));
    }

    /**
     * Immediately populate the blacklist cache after logout/refresh
     * so the next request for the same token hits the cache, not the DB.
     *
     * <p>{@code @CachePut} always executes the method AND updates the cache.
     * This is called AFTER the {@code InvalidatedToken} is already saved to DB.
     *
     * @param jti JWT ID string
     * @return always {@code true} (the cached value)
     */
    @CachePut(value = RedisCacheConfig.CACHE_INVALIDATED_TOKEN, key = "#jti")
    public boolean addToBlacklist(String jti) {
        log.debug("Marking token as blacklisted in cache: jti={}", jti);
        return true;
    }

    // --- 4. Tiện ích lấy User ID từ Token (Dùng cho Socket) ---
    public String getUserIdFromToken(String token) {
        try {
            SignedJWT jwt = verifyToken(token, false);
            return jwt.getJWTClaimsSet().getStringClaim("userId");
        } catch (Exception e) {
            return null;
        }
    }

    // --- 5. Build Scope (Private helper) ---
    private String buildScope(User user) {
        StringJoiner stringJoiner = new StringJoiner(" ");
        if (!CollectionUtils.isEmpty(user.getRoles())) {
            user.getRoles().forEach(role -> {
                stringJoiner.add("ROLE_" + role.getName());
                if (!CollectionUtils.isEmpty(role.getPermissions())) {
                    role.getPermissions().forEach(permission -> {
                        stringJoiner.add(permission.getName());
                    });
                }
            });
        }
        return stringJoiner.toString();
    }
}
