package com.tripjoy.api.dto.request;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerateItineraryRequest {

    @NotBlank(message = "Destination is required")
    private String destination;

    @NotNull(message = "Latitude is required")
    private Double latitude;

    @NotNull(message = "Longitude is required")
    private Double longitude;

    @NotNull(message = "Start date is required")
    private LocalDateTime startDate;

    @NotNull(message = "End date is required")
    private LocalDateTime endDate;

    @Positive(message = "People quantity must be positive")
    private Integer peopleQuantity;

    @PositiveOrZero(message = "Budget estimate must be positive or zero")
    private Double budgetEstimate;

    private Set<String> themes;

    /**
     * Optional: danh sách place_id từ SuggestLocation của group.
     * AI Service sẽ ưu tiên đưa các địa điểm này vào lịch trình nếu phù hợp.
     */
    private List<String> suggestLocations;
}
