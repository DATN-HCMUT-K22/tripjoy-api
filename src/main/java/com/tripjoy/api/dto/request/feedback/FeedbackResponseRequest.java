package com.tripjoy.api.dto.request.feedback;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FeedbackResponseRequest {

    @NotBlank
    @Schema(description = "The admin's reply content to the users", example = "Thank you for your feedback! We are looking into this issue.")
    String description;

    @Schema(description = "New status for the feedback", example = "REPLIED")
    String status;
}