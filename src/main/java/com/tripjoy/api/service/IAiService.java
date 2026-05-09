package com.tripjoy.api.service;

import java.util.List;

import reactor.core.publisher.Mono;
import com.tripjoy.api.dto.ai.AiChatRequestDto;
import com.tripjoy.api.dto.ai.AiChatResponseDto;
import com.tripjoy.api.dto.ai.AiFinalItineraryDto;
import com.tripjoy.api.dto.ai.AiGenerateNotebookRequestDto;
import com.tripjoy.api.dto.ai.AiModifyItineraryRequestDto;
import com.tripjoy.api.dto.ai.AiNotebookResponseDto;
import com.tripjoy.api.dto.ai.AiSuggestLocationRequestDto;
import com.tripjoy.api.dto.ai.AiTravelRequestDto;
import com.tripjoy.api.dto.ai.AiTripItemDto;

public interface IAiService {

    /** POST /generate-itinerary */
    Mono<AiFinalItineraryDto> generateItinerary(AiTravelRequestDto request);

    /** POST /modify-itinerary */
    Mono<AiFinalItineraryDto> modifyItinerary(AiModifyItineraryRequestDto request);

    /** POST /generate-notebook */
    Mono<AiNotebookResponseDto> generateNotebook(AiGenerateNotebookRequestDto request);

    /** POST /chat */
    Mono<AiChatResponseDto> chat(AiChatRequestDto request);

    /** POST /suggest-location — gợi ý các địa điểm thay thế cho 1 TripItem */
    Mono<List<AiTripItemDto>> suggestLocation(AiSuggestLocationRequestDto request);
}
