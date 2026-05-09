package com.tripjoy.api;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordHashTest {

    @Test
    public void generatePasswordHash() {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String rawPassword = "StrongP@ss123";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        System.out.println("-------------------------------------------------");
        System.out.println("Mật khẩu gốc: " + rawPassword);
        System.out.println("Mã Hash BCrypt: " + encodedPassword);
        System.out.println("-------------------------------------------------");
    }
}
