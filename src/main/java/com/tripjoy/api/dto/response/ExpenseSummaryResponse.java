package com.tripjoy.api.dto.response;

import java.math.BigDecimal;
import java.util.List;

import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * Aggregated expense summary for an entire itinerary.
 * Used by the summary endpoint to show who paid what across the trip.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ExpenseSummaryResponse {

    /** Grand total of all expenses in the itinerary. */
    BigDecimal totalAmount;

    /** Breakdown of total paid amount per member. */
    List<UserExpenseSummary> userSummaries;
}
