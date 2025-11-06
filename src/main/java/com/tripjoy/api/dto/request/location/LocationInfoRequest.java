package com.tripjoy.api.dto.request.location;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LocationInfoRequest {

    @NotBlank
    @Schema(description = "Detailed content for the location (e.g., HTML, Markdown)")
    String content;

    @NotBlank
    @Schema(description = "Type of the content (e.g., 'Medical', 'Cultural', 'Recreational')")
    String contentType;
}