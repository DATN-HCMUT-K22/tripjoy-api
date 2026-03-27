package com.tripjoy.api.dto.request.member;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tripjoy.api.enums.GroupRole;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AddMemberRequest {

    @NotNull(message = "User ID is required")
    @JsonProperty("member_id")
    @Schema(
            name = "member_id",
            description = "UUID of the users to be added to the group",
            type = "String",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "a1b2c3d4-e5f6-7890-1234-567890abcdef")
    UUID memberId;

    @NotNull(message = "Role is required")
    @Schema(
            name = "role",
            description = "Assign new role to member",
            type = "string",
            allowableValues = {"LEADER", "CO_LEADER", "MEMBER"},
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "CO_LEADER")
    GroupRole role = GroupRole.MEMBER;
}
