package com.tripjoy.api.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(name = "MediaUploadResponse", description = "Response after uploading a media file to Cloudinary")
public class MediaUploadResponse {

    @Schema(
            description = "HTTP URL of the uploaded media",
            example = "http://res.cloudinary.com/tripjoy/image/upload/v1614770203/tripjoy/avatars/users/img.jpg")
    String url;

    @JsonProperty("secure_url")
    @Schema(
            description = "HTTPS URL of the uploaded media (use this in production)",
            example = "https://res.cloudinary.com/tripjoy/image/upload/v1614770203/tripjoy/avatars/users/img.jpg")
    String secureUrl;

    @JsonProperty("public_id")
    @Schema(
            description = "Cloudinary public ID, used for deletion or further transformations",
            example = "tripjoy/avatars/users/abc123")
    String publicId;

    @Schema(description = "File format after processing", example = "webp")
    String format;

    @Schema(description = "Width in pixels (images only)", example = "1080")
    Integer width;

    @Schema(description = "Height in pixels (images only)", example = "1080")
    Integer height;

    @Schema(description = "File size in bytes", example = "204800")
    Long bytes;

    @Schema(description = "Duration in seconds (videos only)", example = "30.5")
    Double duration;

    @JsonProperty("resource_type")
    @Schema(description = "Resource type: image or video", example = "image")
    String resourceType;
}
