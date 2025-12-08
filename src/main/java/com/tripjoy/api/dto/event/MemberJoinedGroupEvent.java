package com.tripjoy.api.dto.event;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberJoinedGroupEvent {
    private UUID groupId;
    private UUID userId;
}