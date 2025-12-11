package com.tripjoy.api.repository;

import com.tripjoy.api.entity.Group;
import com.tripjoy.api.entity.GroupMember;
import com.tripjoy.api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface GroupMemberRepository extends JpaRepository<GroupMember, UUID> {
    boolean existsByGroupAndUser(Group groupId, User userId);

    List<GroupMember> findByUserId(UUID userId);
}
