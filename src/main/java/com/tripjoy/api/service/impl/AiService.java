package com.tripjoy.api.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import com.tripjoy.api.configuration.AiServiceProperties;
import com.tripjoy.api.dto.ai.AiFinalItineraryDto;
import com.tripjoy.api.dto.ai.AiTravelRequestDto;
import com.tripjoy.api.exception.AppException;
import com.tripjoy.api.exception.ErrorCode;
import com.tripjoy.api.service.IAiService;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiService implements IAiService {

    private final WebClient aiServiceWebClient;
    private final AiServiceProperties aiServiceProperties;

    // The name matches the instance in application.yaml
    private static final String AI_SERVICE_NAME = "aiService";

    @Override
    @CircuitBreaker(name = AI_SERVICE_NAME, fallbackMethod = "generateItineraryFallback")
    @Retry(name = AI_SERVICE_NAME)
    public Mono<AiFinalItineraryDto> generateItinerary(AiTravelRequestDto request) {
        log.info("Sending generate-itinerary request to AI Service for destination: {}", request.getDestinationName());

        return aiServiceWebClient.post()
                .uri("/generate-itinerary")
                .header("X-Internal-Api-Key", aiServiceProperties.getApiKey())
                .bodyValue(request)
                .retrieve()
                .bodyToMono(AiFinalItineraryDto.class)
                .doOnSuccess(response -> log.info("Successfully received itinerary from AI Service"))
                .doOnError(e -> log.error("Error communicating with AI Service: {}", e.getMessage()));
    }

    /**
     * Fallback method called when CircuitBreaker is open or all retries fail.
     * Must have the same signature plus the Throwable it catches.
     */
    public Mono<AiFinalItineraryDto> generateItineraryFallback(AiTravelRequestDto request, Throwable t) {
        log.error("AI Service fallback triggered for generation request. Reason: {}", t.getMessage());
        return Mono.error(new AppException(ErrorCode.AI_SERVICE_UNAVAILABLE));
    }
}
