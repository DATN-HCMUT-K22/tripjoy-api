package com.tripjoy.api.dto.ai;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.*;

/**
 * DTO gửi sang AI Service cho endpoint POST /generate-notebook.
 * Khớp 1:1 với Python model {@code FinalItinerary} mà AI Service nhận vào.
 *
 * <p>AI Service dùng {@code destination} để tra cứu Wikipedia rồi kết hợp
 * với Gemini để tạo ra nội dung Travel Notebook.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiGenerateNotebookRequestDto {

    @JsonProperty("name")
    private String name;

    @JsonProperty("start_date")
    private String startDate;

    @JsonProperty("end_date")
    private String endDate;

    @JsonProperty("people_quantity")
    private Integer peopleQuantity;

    /** Budget estimate as number (VND) */
    @JsonProperty("budget_estimate")
    private Long budgetEstimate;

    @JsonProperty("themes")
    private List<String> themes;

    /**
     * Tên điểm đến — AI dùng để tra Wikipedia.
     * Ví dụ: "Da Lat", "Hoi An", "Nha Trang"
     */
    @JsonProperty("destination")
    private String destination;

    @JsonProperty("trip_items")
    private List<AiTripItemDto> tripItems;
}
