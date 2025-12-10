package com.tripjoy.api.dto.request.member;

import com.tripjoy.api.enums.GroupRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateMemberRoleRequest {

    @NotNull(message = "Role is required")
    @Schema(
            name = "role",
            description = "Assign new role to member",
            type = "string",
            allowableValues = {"LEADER", "CO_LEADER", "MEMBER"},
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "CO_LEADER"
    )
    GroupRole role;
}