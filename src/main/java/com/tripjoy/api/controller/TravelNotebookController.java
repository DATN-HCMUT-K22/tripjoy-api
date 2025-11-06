package com.tripjoy.api.controller;

import com.tripjoy.api.constant.Endpoint;
import com.tripjoy.api.dto.request.TravelNotebookRequest;
import com.tripjoy.api.dto.response.ApiResponse;
import com.tripjoy.api.dto.response.TravelNotebookResponse;
import com.tripjoy.api.service.TravelNotebookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(Endpoint.TravelNotebook.BASE)
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Travel Notebook", description = "Endpoints for managing travel notebooks")
public class TravelNotebookController {

    TravelNotebookService travelNotebookService;

    @Operation(summary = "Create a new travel notebook")
    @PostMapping
    public ApiResponse<TravelNotebookResponse> createNotebook(
            @Valid @RequestBody TravelNotebookRequest request) {

        // return ApiResponse.<TravelNotebookResponse>builder()
        //        .data(travelNotebookService.createNotebook(request))
        //        .build();
        return null; // Placeholder
    }

    @Operation(summary = "Get a single travel notebook by ID")
    @GetMapping(Endpoint.TravelNotebook.ID)
    public ApiResponse<TravelNotebookResponse> getNotebookById(
            @PathVariable String notebookId) {

        // return ApiResponse.<TravelNotebookResponse>builder()
        //        .data(travelNotebookService.getNotebookById(notebookId))
        //        .build();
        return null; // Placeholder
    }

    @Operation(summary = "Update a travel notebook")
    @PutMapping(Endpoint.TravelNotebook.ID)
    public ApiResponse<TravelNotebookResponse> updateNotebook(
            @PathVariable String notebookId,
            @Valid @RequestBody TravelNotebookRequest request) {

        // return ApiResponse.<TravelNotebookResponse>builder()
        //        .data(travelNotebookService.updateNotebook(notebookId, request))
        //        .build();
        return null; // Placeholder
    }

    @Operation(summary = "Delete a travel notebook")
    @DeleteMapping(Endpoint.TravelNotebook.ID)
    public ApiResponse<Void> deleteNotebook(@PathVariable String notebookId) {

        // travelNotebookService.deleteNotebook(notebookId);
        // return ApiResponse.<Void>builder().message("Notebook deleted").build();
        return null; // Placeholder
    }
}