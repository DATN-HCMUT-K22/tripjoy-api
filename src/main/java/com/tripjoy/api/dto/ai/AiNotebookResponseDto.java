package com.tripjoy.api.dto.ai;

import lombok.*;

/**
 * DTO nhận response từ AI Service endpoint POST /generate-notebook.
 * Khớp 1:1 với Python model {@code TravelNotebook}:
 * <pre>
 * @dataclass
 * class TravelNotebook:
 *     name: str
 *     food: str
 *     climate: str
 *     culture: str
 * </pre>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiNotebookResponseDto {
    private String name;
    private String food;
    private String climate;
    private String culture;
}
