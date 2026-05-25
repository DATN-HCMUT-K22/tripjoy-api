package com.tripjoy.api.dto.request;

import jakarta.validation.constraints.NotNull;

import com.tripjoy.api.enums.TripItemStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TripItemStatusRequest {

    @NotNull(message = "INVALID_REQUEST")
    @Schema(name = "status", description = "Trạng thái mới của trip item", example = "CHECKED_IN")
    TripItemStatus status;
}
