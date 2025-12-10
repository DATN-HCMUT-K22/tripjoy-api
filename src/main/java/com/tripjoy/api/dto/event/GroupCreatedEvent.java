package com.tripjoy.api.dto.event;

import com.tripjoy.api.entity.Group;
import com.tripjoy.api.entity.User;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupCreatedEvent {
    private Group group; // Group vừa được tạo
    private User creator; // Người tạo (để add vào chat luôn)
    private List<User> initialMembers;
}