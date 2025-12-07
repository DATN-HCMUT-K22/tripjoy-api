package com.tripjoy.api.service;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.tripjoy.api.configuration.security.JwtUtils;
import com.tripjoy.api.dto.request.auth.AuthenticationRequest;
import com.tripjoy.api.dto.request.auth.IntrospectRequest;
import com.tripjoy.api.dto.request.auth.LogoutRequest;
import com.tripjoy.api.dto.request.auth.RefreshRequest;
import com.tripjoy.api.dto.response.auth.AuthenticationResponse;
import com.tripjoy.api.dto.response.auth.IntrospectResponse;
import com.tripjoy.api.entity.InvalidatedToken;
import com.tripjoy.api.entity.User;
import com.tripjoy.api.exception.AppException;
import com.tripjoy.api.exception.ErrorCode;
import com.tripjoy.api.repository.InvalidatedTokenRepository;
import com.tripjoy.api.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.StringJoiner;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationService {
    UserRepository userRepository;
    InvalidatedTokenRepository invalidatedTokenRepository;
    PasswordEncoder passwordEncoder;
    JwtUtils jwtUtils;

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        var user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        boolean isAuthenticated = passwordEncoder.matches(request.getPassword(), user.getPassword());

        if (!isAuthenticated)
            throw new AppException(ErrorCode.UNAUTHENTICATED);

        // Gọi JwtUtils để tạo token
        var token = jwtUtils.generateToken(user);

        return AuthenticationResponse.builder()
                .token(token)
                .isAuthenticated(isAuthenticated)
                .build();
    }

    public IntrospectResponse introspectToken(IntrospectRequest request) {
        var token = request.getToken();
        boolean isValid = true;
        try {
            // Gọi JwtProvider để kiểm tra
            jwtUtils.verifyToken(token, false);
        } catch (Exception e) {
            isValid = false;
        }
        return IntrospectResponse.builder().isValid(isValid).build();
    }

    public void logout(LogoutRequest request)
        throws JOSEException, ParseException {
        try {
            // Gọi JwtProvider để lấy thông tin token
            var signedToken = jwtUtils.verifyToken(request.getToken(), true);

            String jti = signedToken.getJWTClaimsSet().getJWTID();
            UUID jtiUuid = UUID.fromString(jti);
            Date expiresAt = signedToken.getJWTClaimsSet().getExpirationTime();

            InvalidatedToken invalidatedToken = InvalidatedToken.builder()
                    .id(jtiUuid)
                    .expiresAt(expiresAt)
                    .build();

            invalidatedTokenRepository.save(invalidatedToken);
        } catch (AppException e) {
            log.info("Token already invalidated");
        }
    }

    public AuthenticationResponse refreshToken(RefreshRequest request)
            throws ParseException, JOSEException {
        // Gọi JwtProvider verify token cũ
        var signedToken = jwtUtils.verifyToken(request.getToken(), true);

        var jti = signedToken.getJWTClaimsSet().getJWTID();
        UUID jtiUuid = UUID.fromString(jti);
        var expiresAt = signedToken.getJWTClaimsSet().getExpirationTime();

        // Logout token cũ
        InvalidatedToken invalidatedToken = InvalidatedToken.builder()
                .id(jtiUuid)
                .expiresAt(expiresAt)
                .build();
        invalidatedTokenRepository.save(invalidatedToken);

        var username = signedToken.getJWTClaimsSet().getSubject();
        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // Tạo token mới
        var newToken = jwtUtils.generateToken(user);

        return AuthenticationResponse.builder()
                .token(newToken)
                .isAuthenticated(true)
                .build();
    }
}
