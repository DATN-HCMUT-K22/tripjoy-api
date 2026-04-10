package com.tripjoy.api.service;

import reactor.core.publisher.Mono;
import com.tripjoy.api.dto.ai.AiFinalItineraryDto;
import com.tripjoy.api.dto.ai.AiGenerateNotebookRequestDto;
import com.tripjoy.api.dto.ai.AiModifyItineraryRequestDto;
import com.tripjoy.api.dto.ai.AiNotebookResponseDto;
import com.tripjoy.api.dto.ai.AiTravelRequestDto;

public interface IAiService {
    Mono<AiFinalItineraryDto> generateItinerary(AiTravelRequestDto request);
    Mono<AiFinalItineraryDto> modifyItinerary(AiModifyItineraryRequestDto request);
    Mono<AiNotebookResponseDto> generateNotebook(AiGenerateNotebookRequestDto request);
}

