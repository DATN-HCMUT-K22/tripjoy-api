package com.tripjoy.api.dto.request.member;

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

    @NotNull
    @Schema(
            name = "is_leader",
            description = "Set the member's leader status",
            type = "Boolean",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "true"
    )
    Boolean isLeader;
}