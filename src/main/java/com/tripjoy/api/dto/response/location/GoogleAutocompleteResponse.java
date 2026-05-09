package com.tripjoy.api.dto.response.location;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * Response DTO mapping for Google Places Autocomplete API (New API v1).
 *
 * <p>Maps the JSON response from:
 * POST https://places.googleapis.com/v1/places:autocomplete
 *
 * <pre>
 * {
 *   "suggestions": [
 *     {
 *       "placePrediction": {
 *         "placeId": "ChIJ...",
 *         "text": { "text": "Highlands Coffee - Nguyễn Huệ, Quận 1, Ho Chi Minh City..." },
 *         "structuredFormat": {
 *           "mainText": { "text": "Highlands Coffee - Nguyễn Huệ" },
 *           "secondaryText": { "text": "Quận 1, Hồ Chí Minh, Vietnam" }
 *         },
 *         "types": ["cafe", "food", "point_of_interest"]
 *       }
 *     }
 *   ]
 * }
 * </pre>
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GoogleAutocompleteResponse {

    private List<Suggestion> suggestions;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Suggestion {

        @JsonProperty("placePrediction")
        private PlacePrediction placePrediction;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PlacePrediction {

        @JsonProperty("placeId")
        private String placeId;

        /** Full combined text: "Highlands Coffee, Quận 1, Hồ Chí Minh" */
        @JsonProperty("text")
        private TextContent text;

        /** Structured: mainText (name) + secondaryText (location context) */
        @JsonProperty("structuredFormat")
        private StructuredFormat structuredFormat;

        /** Place types from Google: ["cafe", "food", "point_of_interest", ...] */
        @JsonProperty("types")
        private List<String> types;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TextContent {
        @JsonProperty("text")
        private String text;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class StructuredFormat {
        @JsonProperty("mainText")
        private TextContent mainText;

        @JsonProperty("secondaryText")
        private TextContent secondaryText;
    }
}
