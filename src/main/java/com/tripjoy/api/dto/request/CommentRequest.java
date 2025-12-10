package com.tripjoy.api.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

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

    // Bắt buộc phải biết Comment vào Post nào
    @NotNull
    @JsonProperty("post_id")
    UUID postId;

    // Nếu là reply, gửi kèm ID cha. Nếu là gốc, để null.
    @JsonProperty("parent_comment_id")
    UUID parentCommentId;
}