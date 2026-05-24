package com.tripjoy.api.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.tripjoy.api.dto.response.simple.UserSimpleResponse;
import com.tripjoy.api.enums.ActivityAction;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 * Response DTO for an ActivityLog record.
 * <p>
 * Intentionally does NOT extend BaseResponse because activity logs are immutable
 * audit records — updated_at / updated_by are irrelevant, and created_by is
 * already represented by the nested {@code user} field.
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ActivityLogResponse {

    UUID id;

    /** The user who performed the action. */
    UserSimpleResponse user;

    /** The action that was performed. */
    ActivityAction action;

    /** The type of the entity that was affected (e.g. POST, GROUP, COMMENT). */
    @JsonProperty("entity_type")
    String entityType;

    /** The ID of the entity that was affected. */
    @JsonProperty("entity_id")
    String entityId;

    /**
     * Additional context about the action, stored as a JSON string.
     * Clients can deserialize this field for richer detail.
     */
    String metadata;

    /** The IP address from which the action was performed. May be null. */
    @JsonProperty("ip_address")
    String ipAddress;

    @JsonProperty("created_at")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime createdAt;
}
