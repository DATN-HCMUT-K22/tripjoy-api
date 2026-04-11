package com.tripjoy.api.controller;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;

import com.tripjoy.api.constant.Endpoint;
import com.tripjoy.api.dto.request.SuggestLocationRequest;
import com.tripjoy.api.dto.response.ApiResponse;
import com.tripjoy.api.dto.response.SuggestLocationResponse;
import com.tripjoy.api.service.ISuggestLocationService;
import com.tripjoy.api.utils.SecurityUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping
@RequiredArgsConstructor
@Tag(name = "Location Suggestions", description = "APIs for managing location suggestions in groups")
public class SuggestLocationController {

    private final ISuggestLocationService suggestLocationService;

    @Operation(summary = "Get all location suggestions for a group - OK")
    @GetMapping(Endpoint.Group.LOCATION_SUGGESTIONS)
    public ApiResponse<List<SuggestLocationResponse>> getGroupSuggestions(@PathVariable("groupId") UUID groupId) {

        UUID currentUserId = SecurityUtils.getCurrentUserId();
        List<SuggestLocationResponse> suggestions =
                suggestLocationService.getSuggestionsByGroup(groupId, currentUserId);

        return ApiResponse.<List<SuggestLocationResponse>>builder()
                .data(suggestions)
                .build();
    }

    @Operation(summary = "Suggest a location for the group - OK")
    @PostMapping(Endpoint.Group.LOCATION_SUGGESTIONS)
    public ApiResponse<SuggestLocationResponse> createSuggestion(
            @PathVariable("groupId") UUID groupId, @RequestBody @Valid SuggestLocationRequest request) {

        UUID currentUserId = SecurityUtils.getCurrentUserId();
        SuggestLocationResponse response = suggestLocationService.createSuggestion(groupId, request, currentUserId);

        return ApiResponse.<SuggestLocationResponse>builder().data(response).build();
    }

    @Operation(summary = "Delete a suggestion - Owner or Leader/Co-Leader - OK")
    @DeleteMapping(Endpoint.Group.LOCATION_SUGGESTIONS_ID)
    public ApiResponse<Void> deleteSuggestion(@PathVariable("groupId") UUID groupId, @PathVariable("suggestionId") UUID suggestionId) {

        UUID currentUserId = SecurityUtils.getCurrentUserId();
        suggestLocationService.deleteSuggestion(groupId, suggestionId, currentUserId);

        return ApiResponse.<Void>builder()
                .message("Suggestion deleted successfully")
                .build();
    }
}
