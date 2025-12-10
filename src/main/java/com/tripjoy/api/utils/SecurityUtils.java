package com.tripjoy.api.utils;

import com.tripjoy.api.exception.AppException;
import com.tripjoy.api.exception.ErrorCode;
import lombok.experimental.UtilityClass;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.UUID;

@UtilityClass
public class SecurityUtils {

    public static UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        Object principal = authentication.getPrincipal();

        // Trường hợp dùng OAuth2 Resource Server (JWT)
        if (principal instanceof Jwt jwt) {
            String subject = jwt.getSubject();
            return UUID.fromString(subject);
        }

        // Trường hợp Anonymous hoặc sai cấu hình
        throw new AppException(ErrorCode.UNAUTHENTICATED);
    }
}