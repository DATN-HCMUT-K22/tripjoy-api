package com.tripjoy.api.dto.response.feedback;

import java.util.UUID;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ParentFeedbackSimpleResponse {
    UUID id;
    String title;
    String status;
}
