package com.tripjoy.api.dto.response;

import com.tripjoy.api.dto.response.simple.UserSimpleResponse;
import com.tripjoy.api.enums.GroupRole;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

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