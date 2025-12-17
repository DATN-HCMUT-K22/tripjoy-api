package com.tripjoy.api.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tripjoy.api.dto.response.simple.UserSimpleResponse;
import com.tripjoy.api.enums.NotificationType;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NotificationResponse extends BaseResponse {

    UUID id;

    @JsonProperty("recipient")
    UserSimpleResponse recipient;

    @JsonProperty("actor")
    UserSimpleResponse actor; // Null for system notifications

    @JsonProperty("type")
    NotificationType type;

    @JsonProperty("entity_type")
    String entityType;

    @JsonProperty("entity_id")
    String entityId;

    String title;

    String message;

    String metadata; // JSON string

    @JsonProperty("is_read")
    Boolean isRead;

    @JsonProperty("read_at")
    LocalDateTime readAt;

    @JsonProperty("is_archived")
    Boolean isArchived;

    String priority;
}
