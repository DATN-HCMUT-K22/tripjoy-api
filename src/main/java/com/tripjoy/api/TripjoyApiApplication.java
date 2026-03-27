package com.tripjoy.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableAsync;

import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication // handle annotation ConfigurationProperties in class OpenApiProperties
@ConfigurationPropertiesScan
@EnableAsync
public class TripjoyApiApplication {

    public static void main(String[] args) {

        // Load file .env
        Dotenv dotenv = Dotenv.configure()
                .directory("./") // Tìm file .env ở thư mục gốc dự án
                .ignoreIfMissing() // Nếu không có file (VD: chạy trên Prod) thì bỏ qua, không lỗi
                .load();

        // Bơm các biến trong .env vào System Property của Java
        dotenv.entries().forEach(entry -> {
            System.setProperty(entry.getKey(), entry.getValue());
        });

        SpringApplication.run(TripjoyApiApplication.class, args);
    }
}
