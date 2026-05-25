package com.tripjoy.api.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.tripjoy.api.dto.response.simple.UserSimpleResponse;

import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ExpenseResponse extends BaseResponse {

    UUID id;
    String name;
    String description;
    BigDecimal amount;
    String type;
    String method;

    @JsonProperty("receipt_image_urls")
    List<String> receiptImageUrls;

    /** The member who actually paid for this expense. */
    @JsonProperty("paid_by")
    UserSimpleResponse paidBy;

    /** The actual timestamp when the payment was made. */
    @JsonProperty("paid_at")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime paidAt;

    /**
     * ID of the associated TripItem, if this expense is tied to a specific location visit.
     * Null for general trip expenses.
     */
    @JsonProperty("trip_item_id")
    UUID tripItemId;

    /** The member who recorded/created this expense entry. */
    UserSimpleResponse user;
}
