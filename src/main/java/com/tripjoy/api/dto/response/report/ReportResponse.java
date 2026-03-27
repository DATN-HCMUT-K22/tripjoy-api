package com.tripjoy.api.dto.response.report;

import java.util.UUID;

import com.tripjoy.api.dto.response.BaseResponse;

import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReportResponse extends BaseResponse {
    UUID id;
    String reason;
    String status;
    UUID reportedBy;
    UUID reportedEntityId;
    String reportedEntityType;
}
