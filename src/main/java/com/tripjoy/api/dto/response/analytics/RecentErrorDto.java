package com.tripjoy.api.dto.response.analytics;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RecentErrorDto {

    LocalDateTime timestamp;

    String endpoint;

    @JsonProperty("error_type")
    String errorType;

    Long count;
}
