package com.tripjoy.api.dto.request;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tripjoy.api.enums.TripItemStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TripItemRequest {

    @NotNull(message = "INVALID_REQUEST")
    @JsonProperty("start_time")
    @Schema(
            name = "start_time",
            description = "Start time of this specific trip item (ISO 8601 format)",
            type = "String",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "2026-07-20T10:00:00")
    LocalDateTime startTime;

    @Schema(
            name = "duration",
            description = "Duration of the activity in minutes",
            type = "Integer",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            example = "120")
    Integer duration;
    //
    //    @NotNull
    //    @Schema(
    //            name = "day_order",
    //            description = "The day number of the trip this item belongs to (e.g., 1, 2, 3)",
    //            type = "Integer",
    //            requiredMode = Schema.RequiredMode.REQUIRED,
    //            example = "1"
    //    )
    //    Integer dayOrder;

    @Schema(
            name = "note",
            description = "A note for this trip item",
            type = "String",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            example = "Remember to buy sunscreen.")
    String note;

    @JsonProperty("location_id")
    @Schema(
            name = "location_id",
            description = "UUID of the Location for this trip item (if already in DB)",
            type = "String",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            example = "f1g2h3i4-j5k6-7890-1234-567890lmn-op")
    UUID locationId;

    @JsonProperty("place_id")
    @Schema(
            name = "place_id",
            description = "Google Place ID (used when picking AI suggestion not yet in DB)",
            type = "String",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            example = "ChIJ0T2NLikpdTERgJJ6o5gX1Kw")
    String placeId;

    @Schema(
            name = "status",
            description = "Trạng thái của trip item",
            type = "String",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            example = "PENDING")
    TripItemStatus status;

    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    @Schema(
            name = "rating",
            description = "Đánh giá số sao (từ 1 đến 5)",
            type = "Integer",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            example = "5")
    Integer rating;

    @Schema(
            name = "review",
            description = "Nội dung nhận xét/đánh giá bằng chữ",
            type = "String",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            example = "Chuyến đi tuyệt vời, đồ ăn ngon và phong cảnh đẹp.")
    String review;
}
