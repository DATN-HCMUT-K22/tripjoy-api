package com.tripjoy.api.service;

import reactor.core.publisher.Mono;
import com.tripjoy.api.dto.ai.AiFinalItineraryDto;
import com.tripjoy.api.dto.ai.AiTravelRequestDto;

public interface IAiService {
    Mono<AiFinalItineraryDto> generateItinerary(AiTravelRequestDto request);
}
