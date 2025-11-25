package com.tripjoy.api.dto.request.report;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class HandleReportRequest {

    @NotBlank
    @Schema(
            name = "status",
            description = "The new status of the report",
            type = "String",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "PROCESSED" // e.g., PROCESSED, DISMISSED
    )
    String status;

    @Schema(
            name = "description",
            description = "Admin's private notes on why this action was taken",
            type = "String",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            example = "Users has been warned. Report closed."
    )
    String description;
}