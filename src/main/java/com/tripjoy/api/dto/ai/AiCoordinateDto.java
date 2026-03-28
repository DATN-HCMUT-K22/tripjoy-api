package com.tripjoy.api.dto.ai;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiCoordinateDto {
    private Double latitude;
    private Double longitude;
}
