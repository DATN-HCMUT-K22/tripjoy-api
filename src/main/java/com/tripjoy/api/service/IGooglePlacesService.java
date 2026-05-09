package com.tripjoy.api.service;

import com.tripjoy.api.dto.ai.GooglePlaceDetailsDto;
import com.tripjoy.api.dto.response.location.GoogleAutocompleteResponse;

import reactor.core.publisher.Mono;

/**
 * Contract for Google Places API (New) v1 integration.
 *
 * <p>Docs: https://developers.google.com/maps/documentation/places/web-service/op-overview
 *
 * <p>Two operations:
 * <ol>
 *   <li>{@link #autocomplete} — used by GET /locations/autocomplete to get real-time suggestions while typing.
 *       Returns partial-match predictions without full coordinates (cheap, fast).
 *   <li>{@link #getPlaceDetails} — used after user picks an autocomplete result to fetch full details
 *       (lat/lng, address_components, opening_hours, etc.) before resolving into TripJoy DB.
 * </ol>
 */
public interface IGooglePlacesService {

    /**
     * Calls Google Places Autocomplete (New) API.
     * Session-less mode — suitable for server-side calls.
     *
     * @param input   Partial text input from user (e.g., "Highlands Co")
     * @param cityBias Optional city context to bias results (e.g., "Ho Chi Minh City")
     * @param lat     Optional user latitude for proximity ranking
     * @param lng     Optional user longitude for proximity ranking
     * @return Mono wrapping Google autocomplete response with up to 5 suggestions
     */
    Mono<GoogleAutocompleteResponse> autocomplete(String input, String cityBias, Double lat, Double lng);

    /**
     * Fetches full Place Details for a given Google place_id.
     * Used after user picks an autocomplete suggestion to get lat/lng and full address.
     *
     * @param placeId Google place_id (e.g., "ChIJ0T2NLikpdTERgJJ6o5gX1Kw")
     * @return Mono wrapping full place details
     */
    Mono<GooglePlaceDetailsDto> getPlaceDetails(String placeId);
}
