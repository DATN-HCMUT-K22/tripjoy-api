package com.tripjoy.api.service;

import com.tripjoy.api.dto.request.SuggestLocationRequest;
import com.tripjoy.api.dto.response.SuggestLocationResponse;

import java.util.List;
import java.util.UUID;

public interface ISuggestLocationService {

    List<SuggestLocationResponse> getSuggestionsByGroup(UUID groupId, UUID currentUserId);

    SuggestLocationResponse createSuggestion(UUID groupId, SuggestLocationRequest request, UUID currentUserId);

    void deleteSuggestion(UUID groupId, UUID suggestionId, UUID currentUserId);
}
