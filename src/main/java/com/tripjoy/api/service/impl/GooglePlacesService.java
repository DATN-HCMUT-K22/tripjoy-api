package com.tripjoy.api.service.impl;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.tripjoy.api.dto.ai.GooglePlaceDetailsDto;
import com.tripjoy.api.dto.response.location.GoogleAutocompleteResponse;
import com.tripjoy.api.service.IGooglePlacesService;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * Google Places API (New) v1 client.
 *
 * <p><b>API version:</b> Places API (New) — https://places.googleapis.com/v1/
 *
 * <p><b>Authentication:</b> X-Goog-Api-Key header (API Key, no OAuth required for server calls).
 *
 * <p><b>Key operations:</b>
 * <ul>
 *   <li>Autocomplete: POST /places:autocomplete — real-time suggestions while user types
 *   <li>Place Details: GET /places/{placeId} — full details after user picks a suggestion
 * </ul>
 *
 * <p><b>Error handling:</b> Returns empty Mono on API errors rather than propagating exceptions,
 * so callers can gracefully fall back to DB-only results.
 *
 * <p><b>Timeout:</b> 3 seconds — autocomplete must not block the user experience.
 */
@Slf4j
@Service
public class GooglePlacesService implements IGooglePlacesService {

    private static final Duration TIMEOUT = Duration.ofSeconds(3);

    // Fields requested from Places API (New) for autocomplete resolution
    // Minimize fields to reduce response size and API billing
    private static final String PLACE_DETAILS_FIELDS =
            "id,displayName,formattedAddress,location,types,primaryType,rating,userRatingCount";

    private final WebClient webClient;
    private final String apiKey;

    public GooglePlacesService(
            WebClient.Builder webClientBuilder, @Value("${google.places.api-key:UNCONFIGURED_KEY}") String apiKey) {
        this.webClient = webClientBuilder
                .baseUrl("https://places.googleapis.com/v1")
                .defaultHeader("X-Goog-Api-Key", apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
        this.apiKey = apiKey;
    }

    /**
     * Calls Google Places Autocomplete (New) API.
     *
     * <p>Request: POST /places:autocomplete
     * <p>Body:
     * <pre>{@code
     * {
     *   "input": "Highlands Co",
     *   "locationBias": {
     *     "circle": {
     *       "center": { "latitude": 10.77, "longitude": 106.66 },
     *       "radius": 50000
     *     }
     *   }
     * }
     * }</pre>
     *
     * <p>Returns empty on error — caller falls back to DB results only.
     */
    @Override
    public Mono<GoogleAutocompleteResponse> autocomplete(String input, String cityBias, Double lat, Double lng) {
        if (input == null || input.trim().length() < 2) {
            return Mono.empty();
        }

        log.debug("Google Places autocomplete: input='{}', city='{}', lat={}, lng={}", input, cityBias, lat, lng);

        Map<String, Object> requestBody = buildAutocompleteRequest(input, cityBias, lat, lng);

        return webClient
                .post()
                .uri("/places:autocomplete")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(GoogleAutocompleteResponse.class)
                .timeout(TIMEOUT)
                .doOnNext(resp -> log.debug(
                        "Google autocomplete returned {} suggestions",
                        resp.getSuggestions() != null ? resp.getSuggestions().size() : 0))
                .onErrorResume(WebClientResponseException.class, e -> {
                    log.warn(
                            "Google Places autocomplete API error {}: {}",
                            e.getStatusCode(),
                            e.getResponseBodyAsString());
                    return Mono.empty();
                })
                .onErrorResume(Exception.class, e -> {
                    log.warn("Google Places autocomplete failed: {}", e.getMessage());
                    return Mono.empty();
                });
    }

    /**
     * Fetches full Place Details for a resolved place_id.
     *
     * <p>Request: GET /places/{placeId}?fields=...
     *
     * <p>Returns empty on error — caller creates location from available data.
     */
    @Override
    public Mono<GooglePlaceDetailsDto> getPlaceDetails(String placeId) {
        if (placeId == null || placeId.isBlank()) {
            return Mono.empty();
        }

        log.debug("Fetching Google Place Details for place_id: {}", placeId);

        return webClient
                .get()
                .uri("/places/{placeId}?fields={fields}", placeId, PLACE_DETAILS_FIELDS)
                .retrieve()
                .bodyToMono(GooglePlaceDetailsDto.class)
                .timeout(TIMEOUT)
                .doOnNext(dto -> log.debug("Google Place Details fetched: {}", dto.getDisplayName()))
                .onErrorResume(WebClientResponseException.class, e -> {
                    log.warn(
                            "Google Places Details API error for placeId={}: {} — {}",
                            placeId,
                            e.getStatusCode(),
                            e.getMessage());
                    return Mono.empty();
                })
                .onErrorResume(Exception.class, e -> {
                    log.warn("Google Places Details failed for placeId={}: {}", placeId, e.getMessage());
                    return Mono.empty();
                });
    }

    // ==================== Private helpers ====================

    /**
     * Builds the autocomplete request body.
     * Optionally adds location bias if lat/lng are provided.
     */
    private Map<String, Object> buildAutocompleteRequest(String input, String cityBias, Double lat, Double lng) {
        Map<String, Object> body = new HashMap<>();
        
        // Bias the text input with city context if provided
        String finalInput = input.trim();
        if (cityBias != null && !cityBias.trim().isEmpty()) {
            finalInput = finalInput + ", " + cityBias.trim();
        }
        body.put("input", finalInput);
        
        body.put("languageCode", "vi"); // Vietnamese-biased results for TripJoy VN
        body.put("regionCode", "VN"); // Bias to Vietnam results
        
        // Included top 5 primary types for travel to optimize relevance (Google limit is 5)
        body.put("includedPrimaryTypes", new String[] {
            "tourist_attraction", "restaurant", "cafe", "lodging", "museum"
        });

        // Location bias: user's current position (if available) or city center
        if (lat != null && lng != null) {
            body.put(
                    "locationBias",
                    Map.of(
                            "circle",
                            Map.of(
                                    "center",
                                    Map.of("latitude", lat, "longitude", lng),
                                    "radius",
                                    50000.0 // 50km radius bias
                                    )));
        }

        return body;
    }
}
