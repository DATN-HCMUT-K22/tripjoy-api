package com.tripjoy.api.dto.response.analytics;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TopReporterDto {

    @JsonProperty("user_id")
    UUID userId;

    String username;

    @JsonProperty("reports_submitted")
    Long reportsSubmitted;
}
