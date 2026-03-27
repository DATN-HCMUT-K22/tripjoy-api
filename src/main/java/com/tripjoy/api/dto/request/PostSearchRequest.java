package com.tripjoy.api.dto.request;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(description = "Enterprise search parameters for Posts")
public class PostSearchRequest {

    @Schema(description = "Full-text search keyword for post content")
    String q;

    @Schema(description = "Exact match hashtag")
    String hashtag;

    @JsonProperty("creator_id")
    @Schema(description = "Exact match creator UUID")
    UUID creatorId;

    // --- Itinerary Info ---
    @JsonProperty("itinerary_id")
    @Schema(description = "Exact match itinerary UUID")
    UUID itineraryId;

    @JsonProperty("start_date")
    @Schema(description = "Trip start date (inclusive)")
    LocalDateTime startDate;

    @JsonProperty("end_date")
    @Schema(description = "Trip end date (inclusive)")
    LocalDateTime endDate;

    @Min(0)
    @JsonProperty("min_days")
    @Schema(description = "Minimum trip duration in days")
    Integer minDays;

    @Min(0)
    @JsonProperty("max_days")
    @Schema(description = "Maximum trip duration in days")
    Integer maxDays;

    @Min(0)
    @JsonProperty("min_budget")
    @Schema(description = "Minimum estimated budget")
    BigDecimal minBudget;

    @Min(0)
    @JsonProperty("max_budget")
    @Schema(description = "Maximum estimated budget")
    BigDecimal maxBudget;

    @Min(1)
    @JsonProperty("min_people")
    @Schema(description = "Minimum people quantity")
    Integer minPeople;

    @Min(1)
    @JsonProperty("max_people")
    @Schema(description = "Maximum people quantity")
    Integer maxPeople;

    @JsonProperty("origin_id")
    @Schema(description = "Starting location UUID")
    UUID originId;

    @JsonProperty("destination_id")
    @Schema(description = "Destination location UUID")
    UUID destinationId;

    // --- Pagination & Sorting ---
    @Builder.Default
    @Min(0)
    @Schema(description = "Page number (0-indexed)", defaultValue = "0")
    int page = 0;

    @Builder.Default
    @Min(1)
    @Max(50)
    @Schema(description = "Page size (max 50)", defaultValue = "20")
    int size = 20;

    @Builder.Default
    @Pattern(regexp = "^(relevance|newest)$", message = "Sort must be 'relevance' or 'newest'")
    @Schema(description = "Sort strategy", allowableValues = {"relevance", "newest"}, defaultValue = "relevance")
    String sort = "relevance";
}
