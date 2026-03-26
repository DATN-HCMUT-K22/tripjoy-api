package com.tripjoy.api.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "ai-service")
public class AiServiceProperties {
    private String baseUrl;
    private int timeoutSeconds = 60;
    private int connectTimeoutSeconds = 10;
}
