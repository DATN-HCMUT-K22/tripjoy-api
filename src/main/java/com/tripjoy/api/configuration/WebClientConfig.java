package com.tripjoy.api.configuration;

import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;

import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.RequiredArgsConstructor;
import reactor.netty.http.client.HttpClient;

@Configuration
@RequiredArgsConstructor
public class WebClientConfig {

    private final AiServiceProperties aiServiceProperties;

    @Bean
    public WebClient aiServiceWebClient(WebClient.Builder webClientBuilder) {
        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofSeconds(aiServiceProperties.getTimeoutSeconds()))
                .doOnConnected(
                        conn -> conn.addHandlerLast(new ReadTimeoutHandler(aiServiceProperties.getTimeoutSeconds()))
                                .addHandlerLast(new WriteTimeoutHandler(aiServiceProperties.getTimeoutSeconds())));

        return webClientBuilder
                .baseUrl(aiServiceProperties.getBaseUrl())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .codecs(configurer ->
                        configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024)) // 16MB max response size
                .build();
    }
}
