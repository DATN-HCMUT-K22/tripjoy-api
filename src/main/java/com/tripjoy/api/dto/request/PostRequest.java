package com.tripjoy.api.dto.request;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PostRequest {
    @JsonProperty("media_urls")
    @Schema(
            name = "media_urls",
            description =
                    "List of Cloudinary secure URLs (upload via POST /api/v1/media/upload/image or /video first, then pass the URLs here)",
            type = "array",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            example = "[\"https://res.cloudinary.com/tripjoy/image/upload/v1/tripjoy/posts/img1.webp\"]")
    private List<String> mediaUrls;

    @NotBlank(message = "{not_blank}")
    @Schema(
            name = "content",
            description = "Content of the post",
            type = "String",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "This was an amazing trip! #danang")
    private String content;

    @JsonProperty("itinerary_id")
    @NotNull
    @Schema(
            name = "itinerary_id",
            description = "UUID of the Itinerary this post is linked to (from Create_post table)",
            type = "String",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            example = "c1d2e3f4-g5h6-7890-1234-567890uvwxyz")
    UUID itineraryId;

    @Schema(
            name = "hashtags",
            description = "Set of hashtags for the post (from Post_hashtag table)",
            type = "Array",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            example = "[\"danang\", \"food\", \"beach\"]")
    Set<String> hashtags;
}
