package com.tripjoy.api.dto.request.member;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AddMemberRequest {

    @NotBlank
    @JsonProperty("member_id")
    @Schema(
            name = "member_id",
            description = "UUID of the user to be added to the group",
            type = "String",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "a1b2c3d4-e5f6-7890-1234-567890abcdef"
    )
    String memberId;

    @Schema(
            name = "is_leader",
            description = "Set this member as a group leader",
            type = "Boolean",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            defaultValue = "false",
            example = "false"
    )
    Boolean isLeader = false;
}