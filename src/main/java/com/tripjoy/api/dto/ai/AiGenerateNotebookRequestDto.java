package com.tripjoy.api.dto.ai;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.*;

/**
 * DTO gửi sang AI Service cho endpoint POST /generate-notebook.
 * Khớp 1:1 với Python model {@code FinalItinerary} mà AI Service nhận vào.
 *
 * <p>AI Service sẽ dùng {@code destination} để tra cứu Wikipedia
 * rồi kết hợp với Gemini để tạo ra nội dung Notebook.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiGenerateNotebookRequestDto {

    /** Tên lịch trình */
    private String name;

    @JsonProperty("start_date")
    private String startDate;

    @JsonProperty("end_date")
    private String endDate;

    @JsonProperty("people_quantity")
    private Integer peopleQuantity;

    @JsonProperty("budget_estimate")
    private String budgetEstimate;

    /** Danh sách theme (ví dụ: ["beach", "food"]) */
    private java.util.List<String> themes;

    /**
     * Tên địa điểm đến (tiếng Anh) — AI dùng để tra Wikipedia.
     * Ví dụ: "Da Lat", "Hoi An", "Hanoi"
     */
    private String destination;

    /**
     * Danh sách TripItem — AI không dùng nhiều cho notebook
     * nhưng cần theo đúng contract của Python model.
     */
    @JsonProperty("trip_items")
    private java.util.List<AiTripItemDto> tripItems;
}
