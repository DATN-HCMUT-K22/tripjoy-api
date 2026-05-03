package com.tripjoy.api.service.impl;

import java.time.Duration;
import java.util.List;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import com.tripjoy.api.configuration.AiServiceProperties;
import com.tripjoy.api.dto.ai.AiChatRequestDto;
import com.tripjoy.api.dto.ai.AiChatResponseDto;
import com.tripjoy.api.dto.ai.AiFinalItineraryDto;
import com.tripjoy.api.dto.ai.AiGenerateNotebookRequestDto;
import com.tripjoy.api.dto.ai.AiModifyItineraryRequestDto;
import com.tripjoy.api.dto.ai.AiNotebookResponseDto;
import com.tripjoy.api.dto.ai.AiSuggestLocationRequestDto;
import com.tripjoy.api.dto.ai.AiTravelRequestDto;
import com.tripjoy.api.dto.ai.AiTripItemDto;
import com.tripjoy.api.exception.AppException;
import com.tripjoy.api.exception.ErrorCode;
import com.tripjoy.api.service.IAiService;
import com.tripjoy.api.service.ISystemConfigService;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiService implements IAiService {

    private final WebClient aiServiceWebClient;
    private final AiServiceProperties aiServiceProperties;
    private final ISystemConfigService configService;

    // The name matches the instance in application.yaml
    private static final String AI_SERVICE_NAME = "aiService";

    // =========================================================================
    // 1. GENERATE ITINERARY
    // =========================================================================

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
                .timeout(Duration.ofSeconds(configService.getIntValue("AI_TIMEOUT_SECONDS", 120)))
                .doOnSuccess(response -> log.info("Successfully received itinerary from AI Service"))
                .doOnError(e -> log.error("Error communicating with AI Service: {}", e.getMessage()));
    }

    public Mono<AiFinalItineraryDto> generateItineraryFallback(AiTravelRequestDto request, Throwable t) {
        log.error("AI Service fallback triggered for generation request. Reason: {}", t.getMessage());
        return Mono.error(new AppException(ErrorCode.AI_SERVICE_UNAVAILABLE));
    }

    // =========================================================================
    // 2. MODIFY ITINERARY
    // =========================================================================

    @Override
    @CircuitBreaker(name = AI_SERVICE_NAME, fallbackMethod = "modifyItineraryFallback")
    @Retry(name = AI_SERVICE_NAME)
    public Mono<AiFinalItineraryDto> modifyItinerary(AiModifyItineraryRequestDto request) {
        log.info("Sending modify-itinerary request to AI Service with {} unwanted locations",
                request.getUnwantedLocations() != null ? request.getUnwantedLocations().size() : 0);

        return aiServiceWebClient.post()
                .uri("/modify-itinerary")
                .header("X-Internal-Api-Key", aiServiceProperties.getApiKey())
                .bodyValue(request)
                .retrieve()
                .bodyToMono(AiFinalItineraryDto.class)
                .timeout(Duration.ofSeconds(configService.getIntValue("AI_TIMEOUT_SECONDS", 120)))
                .doOnSuccess(response -> log.info("Successfully received modified itinerary from AI Service"))
                .doOnError(e -> log.error("Error communicating with AI Service (modify): {}", e.getMessage()));
    }

    public Mono<AiFinalItineraryDto> modifyItineraryFallback(AiModifyItineraryRequestDto request, Throwable t) {
        log.error("AI Service fallback triggered for modify request. Reason: {}", t.getMessage());
        return Mono.error(new AppException(ErrorCode.AI_SERVICE_UNAVAILABLE));
    }

    // =========================================================================
    // 3. GENERATE TRAVEL NOTEBOOK
    // =========================================================================

    @Override
    @CircuitBreaker(name = AI_SERVICE_NAME, fallbackMethod = "generateNotebookFallback")
    @Retry(name = AI_SERVICE_NAME)
    public Mono<AiNotebookResponseDto> generateNotebook(AiGenerateNotebookRequestDto request) {
        log.info("Sending generate-notebook request to AI Service for destination: {}", request.getDestination());

        return aiServiceWebClient.post()
                .uri("/generate-notebook")
                .header("X-Internal-Api-Key", aiServiceProperties.getApiKey())
                .bodyValue(request)
                .retrieve()
                .bodyToMono(AiNotebookResponseDto.class)
                .timeout(Duration.ofSeconds(configService.getIntValue("AI_TIMEOUT_SECONDS", 120)))
                .doOnSuccess(response -> log.info("Successfully received travel notebook from AI Service"))
                .doOnError(e -> log.error("Error communicating with AI Service (notebook): {}", e.getMessage()));
    }

    public Mono<AiNotebookResponseDto> generateNotebookFallback(AiGenerateNotebookRequestDto request, Throwable t) {
        log.error("AI Service fallback triggered for notebook request. Reason: {}", t.getMessage());
        return Mono.error(new AppException(ErrorCode.AI_SERVICE_UNAVAILABLE));
    }

    // =========================================================================
    // 4. CHATBOT
    // =========================================================================

    @Override
    @CircuitBreaker(name = AI_SERVICE_NAME, fallbackMethod = "chatFallback")
    @Retry(name = AI_SERVICE_NAME)
    public Mono<AiChatResponseDto> chat(AiChatRequestDto request) {
        log.info("Sending chat request to AI Service, conversation: {}", request.getConversationId());

        // AI service trả về plain String (kết quả của run_agent), không phải JSON object
        // → dùng bodyToMono(String.class) rồi wrap vào AiChatResponseDto
        return aiServiceWebClient.post()
                .uri("/chat")
                .header("X-Internal-Api-Key", aiServiceProperties.getApiKey())
                .bodyValue(request)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(configService.getIntValue("AI_TIMEOUT_SECONDS", 120)))
                .map(raw -> AiChatResponseDto.builder().message(raw).build())
                .doOnSuccess(response -> log.info("Successfully received chat response from AI Service"))
                .doOnError(e -> log.error("Error communicating with AI Service (chat): {}", e.getMessage()));
    }

    public Mono<AiChatResponseDto> chatFallback(AiChatRequestDto request, Throwable t) {
        log.error("AI Service fallback triggered for chat request. Reason: {}", t.getMessage());
        return Mono.error(new AppException(ErrorCode.AI_SERVICE_UNAVAILABLE));
    }

    // =========================================================================
    // 5. SUGGEST LOCATIONS  (plural — khớp với URL /suggest-locations)
    // =========================================================================

    @Override
    @CircuitBreaker(name = AI_SERVICE_NAME, fallbackMethod = "suggestLocationFallback")
    @Retry(name = AI_SERVICE_NAME)
    public Mono<List<AiTripItemDto>> suggestLocation(AiSuggestLocationRequestDto request) {
        log.info("Sending suggest-locations request to AI Service for unwanted location: {}",
                request.getUnwantedLocation() != null ? request.getUnwantedLocation().getLocationName() : "null");

        return aiServiceWebClient.post()
                .uri("/suggest-locations")   // plural — khớp với @app.post("/suggest-locations") trong server.py
                .header("X-Internal-Api-Key", aiServiceProperties.getApiKey())
                .bodyValue(request)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<AiTripItemDto>>() {})
                .timeout(Duration.ofSeconds(configService.getIntValue("AI_TIMEOUT_SECONDS", 120)))
                .doOnSuccess(response -> log.info("Successfully received {} suggested locations from AI Service",
                        response != null ? response.size() : 0))
                .doOnError(e -> log.error("Error communicating with AI Service (suggest-locations): {}", e.getMessage()));
    }

    public Mono<List<AiTripItemDto>> suggestLocationFallback(AiSuggestLocationRequestDto request, Throwable t) {
        log.error("AI Service fallback triggered for suggest-locations request. Reason: {}", t.getMessage());
        return Mono.error(new AppException(ErrorCode.AI_SERVICE_UNAVAILABLE));
    }
}
