package com.tripjoy.api.repository;

import com.tripjoy.api.entity.Group;
import com.tripjoy.api.entity.GroupMember;
import com.tripjoy.api.entity.User;
import com.tripjoy.api.enums.GroupRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface GroupMemberRepository extends JpaRepository<GroupMember, UUID> {
    boolean existsByGroupAndUser(Group groupId, User userId);

    List<GroupMember> findByUserId(UUID userId);

    // Check if user has LEADER or CO_LEADER role
    @Query("SELECT CASE WHEN COUNT(gm) > 0 THEN true ELSE false END " +
            "FROM GroupMember gm " +
            "WHERE gm.group = :group AND gm.user = :user " +
            "AND (gm.role = 'LEADER' OR gm.role = 'CO_LEADER')")
    boolean hasLeadershipRole(@Param("group") Group group, @Param("user") User user);

    // Get all members of a group, sorted by role (LEADER first, then CO_LEADER,
    // then MEMBER)
    List<GroupMember> findByGroupOrderByRoleAsc(Group group);
}
