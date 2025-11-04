package com.tripjoy.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CommentRequest {

    @NotBlank
    @Schema(
            name = "content",
            description = "The text content of the comment",
            type = "String",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "Bài viết này hay quá!"
    )
    String content;
}