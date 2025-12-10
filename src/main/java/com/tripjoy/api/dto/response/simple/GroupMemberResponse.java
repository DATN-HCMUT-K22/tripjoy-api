package com.tripjoy.api.dto.response.simple;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tripjoy.api.enums.GroupRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GroupMemberResponse {

    // Lồng thông tin users
    UserSimpleResponse user;

    GroupRole role;

    @JsonProperty("join_at")
    LocalDateTime joinAt;
}