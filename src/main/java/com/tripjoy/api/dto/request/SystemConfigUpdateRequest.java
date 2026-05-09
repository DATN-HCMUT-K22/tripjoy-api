package com.tripjoy.api.dto.request;

import jakarta.validation.constraints.NotBlank;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SystemConfigUpdateRequest {
    @NotBlank(message = "Config value is required")
    String value;
    
    String description;
}
