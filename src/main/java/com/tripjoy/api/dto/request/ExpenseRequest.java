package com.tripjoy.api.dto.request;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ExpenseRequest {

    @NotBlank(message = "INVALID_REQUEST")
    @Schema(
            name = "name",
            description = "Name of the expense",
            type = "String",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "Dinner at Hai San Be Man")
    String name;

    @Schema(
            name = "description",
            description = "Detailed description of the expense",
            type = "String",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            example = "Fresh seafood dinner")
    String description;

    @NotNull(message = "INVALID_REQUEST")
    @Schema(
            name = "amount",
            description = "Cost of the expense",
            type = "Number",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "1500000.00")
    BigDecimal amount;

    @Schema(
            name = "type",
            description = "Category of the expense (e.g., Food, Transport, Hotel)",
            type = "String",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            example = "Food")
    String type;

    @Schema(
            name = "method",
            description = "Payment method (e.g., Cash, Credit Card, Transfer)",
            type = "String",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            example = "Credit Card")
    String method;

    @JsonProperty("receipt_image_urls")
    @Schema(
            name = "receipt_image_urls",
            description = "List of image URLs serving as receipt/invoice evidence (uploaded to Cloudinary by frontend)",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            example = "[\"https://res.cloudinary.com/demo/image/upload/receipt1.jpg\"]")
    List<String> receiptImageUrls;

    @JsonProperty("paid_by_id")
    @Schema(
            name = "paid_by_id",
            description = "UUID of the member who actually paid. Defaults to the current user if not provided.",
            type = "String",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
    UUID paidById;

    @JsonProperty("paid_at")
    @Schema(
            name = "paid_at",
            description = "Actual timestamp when the payment was made (ISO 8601). Defaults to current time if not provided.",
            type = "String",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            example = "2026-07-20T19:30:00")
    LocalDateTime paidAt;

    @JsonProperty("trip_item_id")
    @Schema(
            name = "trip_item_id",
            description = "Optional UUID of the TripItem this expense is associated with. Leave null for general trip expenses.",
            type = "String",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            example = "f1g2h3i4-j5k6-7890-abcd-567890lmnopq")
    UUID tripItemId;
}
