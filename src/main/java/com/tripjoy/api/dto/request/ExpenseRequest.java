package com.tripjoy.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

    @NotBlank
    @Schema(
            name = "name",
            description = "Name of the expense",
            type = "String",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "Dinner at Hai San Be Man"
    )
    String name;

    @Schema(
            name = "description",
            description = "Detailed description of the expense",
            type = "String",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            example = "Fresh seafood dinner"
    )
    String description;

    @NotNull
    @Schema(
            name = "amount",
            description = "Cost of the expense",
            type = "Double",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "1500000.0"
    )
    Double amount;

    @Schema(
            name = "type",
            description = "Category of the expense",
            type = "String",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            example = "Food"
    )
    String type;

    @Schema(
            name = "method",
            description = "Payment method",
            type = "String",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            example = "Credit Card"
    )
    String method;

    // get user auth info from token (not from request body) -> this is user added expense
}