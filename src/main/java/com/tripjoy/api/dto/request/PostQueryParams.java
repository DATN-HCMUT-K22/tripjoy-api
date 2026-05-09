package com.tripjoy.api.dto.request;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * Unified query parameters for {@code GET /posts}.
 *
 * <p>All fields are optional. When no fields are provided, the endpoint returns a paginated
 * feed of all active posts ordered by creation date (newest first).
 *
 * <p>Multiple criteria combine as AND conditions.
 *
 * <p>Pagination and sort are handled by Spring {@code Pageable} at the controller layer;
 * this DTO focuses purely on <b>domain filter criteria</b>.
 *
 * <p>Replaces the old split between {@code PostSearchRequest} + implicit "no filter" behaviour.
 *
 * <p>Binding: {@code @ModelAttribute} — all fields from HTTP query params (GET request).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(description = "Query parameters for filtering/searching posts (all fields optional)")
public class PostQueryParams {

    // ==================== Text Search ====================

    @Schema(description = "Full-text search keyword on post content", example = "Đà Nẵng cafe")
    String q;

    // ==================== Social Filters ====================

    @Schema(description = "Filter by exact hashtag (with or without #)", example = "dulich")
    String hashtag;

    @JsonProperty("creator_id")
    @Schema(description = "Filter by creator UUID", example = "550e8400-e29b-41d4-a716-446655440000")
    UUID creatorId;

    // ==================== Itinerary Filters ====================

    @JsonProperty("itinerary_id")
    @Schema(description = "Filter by attached itinerary UUID")
    UUID itineraryId;

    @JsonProperty("start_date")
    @Schema(description = "Trip start date (inclusive), ISO-8601", example = "2025-06-01T00:00:00")
    LocalDateTime startDate;

    @JsonProperty("end_date")
    @Schema(description = "Trip end date (inclusive), ISO-8601", example = "2025-06-30T23:59:59")
    LocalDateTime endDate;

    @Min(0)
    @JsonProperty("min_days")
    @Schema(description = "Minimum trip duration in days", example = "3")
    Integer minDays;

    @Min(0)
    @JsonProperty("max_days")
    @Schema(description = "Maximum trip duration in days", example = "7")
    Integer maxDays;

    @Min(0)
    @JsonProperty("min_budget")
    @Schema(description = "Minimum estimated budget (VND)", example = "500000")
    BigDecimal minBudget;

    @Min(0)
    @JsonProperty("max_budget")
    @Schema(description = "Maximum estimated budget (VND)", example = "5000000")
    BigDecimal maxBudget;

    @Min(1)
    @JsonProperty("min_people")
    @Schema(description = "Minimum group size", example = "2")
    Integer minPeople;

    @Min(1)
    @JsonProperty("max_people")
    @Schema(description = "Maximum group size", example = "10")
    Integer maxPeople;

    @JsonProperty("origin_id")
    @Schema(description = "Filter by origin location UUID")
    UUID originId;

    @JsonProperty("destination_id")
    @Schema(description = "Filter by destination location UUID")
    UUID destinationId;

    // ==================== Sort strategy ====================

    @Builder.Default
    @Pattern(regexp = "^(relevance|newest)$", message = "Sort must be 'relevance' or 'newest'")
    @Schema(
            description =
                    """
			Sort strategy:
			- `relevance`: FTS ranking (only meaningful when `q` is provided)
			- `newest`: sort by creation date desc (default)
			""",
            allowableValues = {"relevance", "newest"},
            defaultValue = "newest")
    String sort = "newest";

    // ==================== Convenience methods ====================

    /** Returns true when no filter criteria are set — used to skip expensive FTS query. */
    public boolean isEmpty() {
        return q == null
                && hashtag == null
                && creatorId == null
                && itineraryId == null
                && startDate == null
                && endDate == null
                && minDays == null
                && maxDays == null
                && minBudget == null
                && maxBudget == null
                && minPeople == null
                && maxPeople == null
                && originId == null
                && destinationId == null;
    }

    /** Normalized keyword — trims and returns null if blank. */
    public String normalizedKeyword() {
        return (q != null && !q.trim().isBlank()) ? q.trim() : null;
    }

    /** Normalized hashtag — removes leading '#' if present. */
    public String normalizedHashtag() {
        if (hashtag == null) return null;
        String h = hashtag.trim();
        return h.startsWith("#") ? h.substring(1) : h;
    }
}
