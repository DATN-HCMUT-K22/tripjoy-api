package com.tripjoy.api.service.impl;

import com.nimbusds.jose.*;
import com.tripjoy.api.configuration.security.JwtUtils;
import com.tripjoy.api.constant.PredefinedRole;
import com.tripjoy.api.service.IAuthenticationService;
import com.tripjoy.api.dto.request.UserCreationRequest;
import com.tripjoy.api.dto.request.auth.AuthenticationRequest;
import com.tripjoy.api.dto.request.auth.IntrospectRequest;
import com.tripjoy.api.dto.request.auth.LogoutRequest;
import com.tripjoy.api.dto.request.auth.RefreshRequest;
import com.tripjoy.api.dto.response.UserResponse;
import com.tripjoy.api.dto.response.auth.AuthenticationResponse;
import com.tripjoy.api.dto.response.auth.IntrospectResponse;
import com.tripjoy.api.entity.InvalidatedToken;
import com.tripjoy.api.entity.Role;
import com.tripjoy.api.entity.User;
import com.tripjoy.api.exception.AppException;
import com.tripjoy.api.exception.ErrorCode;
import com.tripjoy.api.mapper.UserMapper;
import com.tripjoy.api.repository.InvalidatedTokenRepository;
import com.tripjoy.api.repository.RoleRepository;
import com.tripjoy.api.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationService implements IAuthenticationService {
    UserRepository userRepository;
    InvalidatedTokenRepository invalidatedTokenRepository;
    PasswordEncoder passwordEncoder;
    JwtUtils jwtUtils;
    UserMapper userMapper;
    RoleRepository roleRepository;

    @Transactional
    public UserResponse register(UserCreationRequest request) {
        // 1. Kiểm tra tồn tại (Username & Email)
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.EMAIL_EXISTED);
        }

        // 2. Map Request -> Entity
        User user = userMapper.toUser(request);

        // 3. Mã hóa mật khẩu
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // 4. Gán Role mặc định (USER)
        // Tìm role tên là "USER" trong DB, nếu chưa có thì lỗi server
        Role userRole = roleRepository.findByName(PredefinedRole.USER_ROLE)
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));

        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        user.setRoles(roles);

        // Set các giá trị mặc định khác
        // SoftDeleteInfo is already initialized by default
        user.setIsLocked(false);
        user.setIsEmailVerified(false); // Mới đăng ký thì chưa verify
        user.setCredits(0L);

        // 5. Lưu xuống DB
        return userMapper.toUserResponse(userRepository.save(user));
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        var user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        boolean isAuthenticated = passwordEncoder.matches(request.getPassword(), user.getPassword());

        if (!isAuthenticated)
            throw new AppException(ErrorCode.UNAUTHENTICATED);

        var accessToken = jwtUtils.generateToken(user);
        var refreshToken = jwtUtils.generateRefreshToken(user);

        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(3600L)
                .isAuthenticated(true)
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
        var signedToken = jwtUtils.verifyToken(request.getToken(), true);

        String tokenType = signedToken.getJWTClaimsSet().getStringClaim("type");
        if (!"refresh".equals(tokenType)) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        var jti = signedToken.getJWTClaimsSet().getJWTID();
        UUID jtiUuid = UUID.fromString(jti);
        var expiresAt = signedToken.getJWTClaimsSet().getExpirationTime();

        InvalidatedToken invalidatedToken = InvalidatedToken.builder()
                .id(jtiUuid)
                .expiresAt(expiresAt)
                .build();
        invalidatedTokenRepository.save(invalidatedToken);

        var username = signedToken.getJWTClaimsSet().getSubject();
        var user = userRepository.findById(UUID.fromString(username))
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        var newAccessToken = jwtUtils.generateToken(user);
        var newRefreshToken = jwtUtils.generateRefreshToken(user);

        return AuthenticationResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .expiresIn(3600L)
                .isAuthenticated(true)
                .build();
    }
}
