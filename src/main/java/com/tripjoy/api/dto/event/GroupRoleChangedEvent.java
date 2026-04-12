package com.tripjoy.api.dto.event;

import com.tripjoy.api.entity.Group;
import com.tripjoy.api.entity.User;
import com.tripjoy.api.enums.GroupRole;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GroupRoleChangedEvent {
    private final Group group;
    private final User actor;
    private final User targetUser;
    private final GroupRole oldRole;
    private final GroupRole newRole;
}
