package com.tripjoy.api.dto.response.analytics;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DailyTrendDto {

    LocalDate date;

    Long count;
}
