package com.tripjoy.api.service;

import reactor.core.publisher.Mono;
import com.tripjoy.api.dto.ai.GooglePlaceDetailsDto;

public interface IGooglePlacesService {
    Mono<GooglePlaceDetailsDto> getPlaceDetails(String placeId);
}
