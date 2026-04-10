package com.tripjoy.api.controller;

import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.tripjoy.api.constant.Endpoint;
import com.tripjoy.api.dto.response.ApiResponse;
import com.tripjoy.api.dto.response.TravelNotebookResponse;
import com.tripjoy.api.service.ITravelNotebookService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping(Endpoint.TravelNotebook.BASE)
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Travel Notebook", description = "AI-powered travel guide: food, climate, culture for each itinerary")
public class TravelNotebookController {

    ITravelNotebookService travelNotebookService;

    // =========================================================================
    // AI ENDPOINT
    // =========================================================================

    @Operation(
        summary = "AI Generate Travel Notebook",
        description = "Gọi AI Service (Gemini via Vertex AI) để sinh Travel Notebook "
            + "cho lịch trình: ẩm thực, khí hậu, văn hóa. "
            + "Nếu notebook đã tồn tại thì nội dung sẽ được cập nhật lại."
    )
    @PostMapping(Endpoint.TravelNotebook.AI_GENERATE)
    public ResponseEntity<ApiResponse<TravelNotebookResponse>> generateNotebook(
            @PathVariable UUID itineraryId) {

        TravelNotebookResponse response = travelNotebookService.generateByItinerary(itineraryId);

        return ResponseEntity.ok(
                ApiResponse.<TravelNotebookResponse>builder()
                        .message("Travel notebook generated successfully by AI")
                        .data(response)
                        .build());
    }

    // =========================================================================
    // QUERY ENDPOINTS
    // =========================================================================

    @Operation(summary = "Get travel notebook for a specific itinerary")
    @GetMapping(Endpoint.TravelNotebook.BY_ITINERARY)
    public ApiResponse<TravelNotebookResponse> getNotebookByItinerary(
            @PathVariable UUID itineraryId) {
        return ApiResponse.<TravelNotebookResponse>builder()
                .data(travelNotebookService.getByItineraryId(itineraryId))
                .build();
    }

    @Operation(summary = "Get a single travel notebook by ID")
    @GetMapping(Endpoint.TravelNotebook.ID)
    public ApiResponse<TravelNotebookResponse> getNotebookById(@PathVariable UUID notebookId) {
        return ApiResponse.<TravelNotebookResponse>builder()
                .data(travelNotebookService.getById(notebookId))
                .build();
    }
}

