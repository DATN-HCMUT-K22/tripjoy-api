package com.tripjoy.api.dto.response;

import java.util.UUID;

import com.tripjoy.api.dto.response.simple.UserSimpleResponse;
import com.tripjoy.api.enums.GroupRole;

import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GroupMemberResponse extends BaseResponse {

    UUID id;

    UserSimpleResponse user;

    GroupRole role; // LEADER, CO_LEADER, MEMBER
}
