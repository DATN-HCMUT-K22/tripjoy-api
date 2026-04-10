package com.tripjoy.api.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import com.tripjoy.api.dto.ai.GooglePlaceDetailsDto;
import com.tripjoy.api.service.IGooglePlacesService;

@Slf4j
@Service
public class GooglePlacesService implements IGooglePlacesService {

    private final WebClient webClient;
    private final String apiKey;

    public GooglePlacesService(WebClient.Builder webClientBuilder, 
                                 @Value("${google.places.api-key:UNCONFIGURED_KEY}") String apiKey) {
        // Build a dedicated web client for Google APIs
        this.webClient = webClientBuilder
                .baseUrl("https://places.googleapis.com/v1/places")
                .build();
        this.apiKey = apiKey;
    }

    @Override
    public Mono<GooglePlaceDetailsDto> getPlaceDetails(String placeId) {
        log.info("Fetching place details from Google Places API for place_id: {}", placeId);
        
        return webClient.get()
                .uri("/{placeId}?fields=id,displayName,formattedAddress,location,types,primaryType", placeId)
                .header("X-Goog-Api-Key", apiKey)
                .retrieve()
                .bodyToMono(GooglePlaceDetailsDto.class)
                .doOnError(e -> log.error("Error fetching Google Place Details for place_id {}: {}", placeId, e.getMessage()));
    }
}
