package com.tripjoy.api.dto.event;

import com.tripjoy.api.entity.Group;
import com.tripjoy.api.entity.User;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GroupLeadershipTransferredEvent {
    private final Group group;
    private final User oldLeader;
    private final User newLeader;
}
