package com.tripjoy.api.configuration;

import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.tripjoy.api.constant.AppConstants;
import com.tripjoy.api.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        seedBotUser();
    }

    private void seedBotUser() {
        if (!userRepository.existsById(AppConstants.TRIPJOY_AI_USER_ID)) {
            String sql =
                    "INSERT INTO users (id, username, full_name, email, password, bio, avatar_url, is_email_verified, is_locked, created_at, updated_at) "
                            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())";

            jdbcTemplate.update(
                    sql,
                    AppConstants.TRIPJOY_AI_USER_ID,
                    "tripjoy_ai",
                    "TripJoy AI",
                    "ai@tripjoy.com",
                    passwordEncoder.encode("SecureAiPassword!2026"),
                    "Tôi là trợ lý AI chuyên nghiệp của TripJoy. Tag @Tripjoy để được hỗ trợ!",
                    "https://res.cloudinary.com/dv6tzvj3e/image/upload/v1714101826/tripjoy_ai_avatar.png",
                    true,
                    false);

            log.info("Seeded TripJoy AI Bot User with ID: {}", AppConstants.TRIPJOY_AI_USER_ID);
        } else {
            log.info("TripJoy AI Bot User already exists.");
        }
    }
}
