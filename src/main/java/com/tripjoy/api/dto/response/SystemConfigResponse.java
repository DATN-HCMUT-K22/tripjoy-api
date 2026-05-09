package com.tripjoy.api.dto.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SystemConfigResponse {
    String key;
    String value;

    @JsonProperty("data_type")
    String dataType;

    String group;
    String description;

    @JsonProperty("updated_at")
    LocalDateTime updatedAt;

    @JsonProperty("updated_by")
    String updatedBy;
}
