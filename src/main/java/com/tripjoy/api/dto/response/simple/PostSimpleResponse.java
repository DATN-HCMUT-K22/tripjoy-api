package com.tripjoy.api.dto.response.simple;

import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tripjoy.api.enums.PostVisibility;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PostSimpleResponse {

    UUID id;

    @Schema(description = "Short snippet of the post content")
    @JsonProperty("content_snippet")
    String contentSnippet;

    @Schema(description = "First media URL of the post to act as a thumbnail")
    @JsonProperty("thumbnail_url")
    String thumbnailUrl;

    @Schema(description = "Location name associated with the post/itinerary")
    @JsonProperty("location_name")
    String locationName;

    @Schema(description = "List of hashtags")
    Set<String> hashtags;

    @Schema(description = "Visibility of the post")
    PostVisibility visibility;

    @JsonProperty("author")
    UserSimpleResponse author;
}
