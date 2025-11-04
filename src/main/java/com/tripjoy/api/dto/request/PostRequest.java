package com.tripjoy.api.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PostRequest {

    @JsonProperty("media_url")
    @NotBlank(message = "{not_blank}")
    @Schema(
            name = "media_url",
            description = "Media file's url of the blog",
            type = "String",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "https://res.cloudinary.com/your_cloud_name/image/upload/v1614770203/mbjf0dum95iijqwrxdzx.jpg"
    )
    private String mediaUrl;

    @NotBlank(message = "{not_blank}")
    @Schema(
            name = "content",
            description = "Content of the post",
            type = "String",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "This was an amazing trip! #danang"
    )
    private String content;

    @JsonProperty("itinerary_id")
    @Schema(
            name = "itinerary_id",
            description = "UUID of the Itinerary this post is linked to (from Create_post table)",
            type = "String",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            example = "c1d2e3f4-g5h6-7890-1234-567890uvwxyz"
    )
    String itineraryId;

    @Schema(
            name = "hashtags",
            description = "Set of hashtags for the post (from Post_hashtag table)",
            type = "Array",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            example = "[\"danang\", \"food\", \"beach\"]"
    )
    Set<String> hashtags;
}
