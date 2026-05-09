package com.tripjoy.api.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.tripjoy.api.entity.Group;
import com.tripjoy.api.entity.GroupMember;
import com.tripjoy.api.entity.User;

public interface GroupMemberRepository extends JpaRepository<GroupMember, UUID> {
    boolean existsByGroupAndUser(Group groupId, User userId);

    List<GroupMember> findByUserId(UUID userId);

    // Check if user has LEADER or CO_LEADER role
    @Query("SELECT CASE WHEN COUNT(gm) > 0 THEN true ELSE false END " + "FROM GroupMember gm "
            + "WHERE gm.group = :group AND gm.user = :user "
            + "AND (gm.role = 'LEADER' OR gm.role = 'CO_LEADER')")
    boolean hasLeadershipRole(@Param("group") Group group, @Param("user") User user);

    // Get all members of a group, sorted by role (LEADER first, then CO_LEADER,
    // then MEMBER)
    List<GroupMember> findByGroupOrderByRoleAsc(Group group);

    // Find specific member in a group
    Optional<GroupMember> findByGroupAndUser(Group group, User user);

    @Query("SELECT gm FROM GroupMember gm WHERE gm.group.id = :groupId AND gm.user.id = :userId")
    Optional<GroupMember> findByGroupIdAndUserId(@Param("groupId") UUID groupId, @Param("userId") UUID userId);

    // === FILTER DELETED RECORDS ===

    @Query(
            "SELECT gm FROM GroupMember gm WHERE gm.group = :group AND gm.softDeleteInfo.isDeleted = false ORDER BY gm.role ASC")
    List<GroupMember> findByGroupAndNotDeletedOrderByRoleAsc(@Param("group") Group group);

    // Optimized EntityGraph: Fetch the group and associated user in a single join.
    // Avoid deep fetching multiple collections here to prevent Cartesian Product performance issues.
    // Deep collections (members, itineraries) are handled via @BatchSize in the entity for optimal performance.
    @EntityGraph(attributePaths = {"group", "user"})
    @Query(
            "SELECT gm FROM GroupMember gm WHERE gm.user.id = :userId AND gm.softDeleteInfo.isDeleted = false ORDER BY gm.group.createdAt DESC")
    List<GroupMember> findByUserIdAndNotDeleted(@Param("userId") UUID userId);

    @Query(
            "SELECT gm FROM GroupMember gm WHERE gm.group = :group AND gm.user = :user AND gm.softDeleteInfo.isDeleted = false")
    java.util.Optional<GroupMember> findByGroupAndUserAndNotDeleted(
            @Param("group") Group group, @Param("user") User user);

    // === SOFT DELETE CASCADE METHODS ===

    /**
     * Bulk soft delete all members of a group
     */
    @Modifying
    @Query("UPDATE GroupMember gm " + "SET gm.softDeleteInfo.isDeleted = true, "
            + "    gm.softDeleteInfo.deletedAt = :deletedAt, "
            + "    gm.softDeleteInfo.deletedBy = :deletedBy "
            + "WHERE gm.group.id = :groupId")
    int softDeleteByGroupId(
            @Param("groupId") UUID groupId,
            @Param("deletedAt") LocalDateTime deletedAt,
            @Param("deletedBy") String deletedBy);

    /**
     * Bulk restore all members of a group
     */
    @Modifying
    @Query("UPDATE GroupMember gm " + "SET gm.softDeleteInfo.isDeleted = false, "
            + "    gm.softDeleteInfo.deletedAt = null, "
            + "    gm.softDeleteInfo.deletedBy = null "
            + "WHERE gm.group.id = :groupId")
    int restoreByGroupId(@Param("groupId") UUID groupId);
}
