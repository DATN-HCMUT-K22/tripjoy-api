package com.tripjoy.api.entity;

import jakarta.persistence.*;

import com.tripjoy.api.entity.embeddable.SoftDeleteInfo;
import com.tripjoy.api.enums.GroupRole;

import lombok.*;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "group_member",
        indexes = {
            @Index(name = "idx_group_member_lookup", columnList = "group_id, user_id"),
            @Index(name = "idx_user_groups", columnList = "user_id, is_deleted")
        })
public class GroupMember extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @Enumerated(EnumType.STRING)
    private GroupRole role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Embedded
    @Builder.Default
    private SoftDeleteInfo softDeleteInfo = new SoftDeleteInfo();
}
