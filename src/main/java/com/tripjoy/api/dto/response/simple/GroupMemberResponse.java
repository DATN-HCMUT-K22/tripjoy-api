package com.tripjoy.api.dto.response.simple;

import com.fasterxml.jackson.annotation.JsonProperty;
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

    @JsonProperty("is_leader")
    Boolean isLeader;

    @JsonProperty("join_at")
    LocalDateTime joinAt;
}