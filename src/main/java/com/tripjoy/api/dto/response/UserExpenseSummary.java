package com.tripjoy.api.dto.response;

import java.math.BigDecimal;

import com.tripjoy.api.dto.response.simple.UserSimpleResponse;

import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * Summary of total paid amount for a single member within an itinerary.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserExpenseSummary {

    UserSimpleResponse user;

    /** Total amount paid by this user across all expenses in the itinerary. */
    BigDecimal totalPaid;

    /** Number of expenses paid by this user. */
    Long expenseCount;
}
