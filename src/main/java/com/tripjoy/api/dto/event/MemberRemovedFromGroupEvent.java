package com.tripjoy.api.dto.event;

import com.tripjoy.api.entity.Group;
import com.tripjoy.api.entity.User;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class MemberRemovedFromGroupEvent {
    private Group group;
    private User removedUser; // The user who was removed
    private User removedByUser; // The user who performed the removal (LEADER/CO_LEADER)
}
