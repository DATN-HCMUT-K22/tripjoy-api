package com.tripjoy.api.dto.ai;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.*;

/**
 * DTO nhận response từ AI Service endpoint POST /generate-notebook.
 * Khớp 1:1 với Python model {@code TravelNotebook}:
 * 
 * <pre>
 * @dataclass
 * class TravelNotebook:
 *     name: str
 *     food: str
 *     climate: str
 *     culture: str
 *     emergency_contacts: str
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

    /** Số điện thoại khẩn cấp, bệnh viện, đại sứ quán — do AI sinh ra */
    @JsonProperty("emergency_contacts")
    private String emergencyContacts;
}
