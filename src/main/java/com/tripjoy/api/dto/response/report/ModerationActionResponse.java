package com.tripjoy.api.dto.response.report;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tripjoy.api.dto.response.simple.UserSimpleResponse;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ModerationActionResponse {

    UUID id;

    @JsonProperty("moderated_user")
    UserSimpleResponse moderatedUser;

    @JsonProperty("admin")
    UserSimpleResponse admin; // BA who performed the action

    @JsonProperty("action_type")
    String actionType;

    @JsonProperty("created_at")
    LocalDateTime createdAt;

    String note;
}
