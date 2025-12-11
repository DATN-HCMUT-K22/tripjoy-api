package com.tripjoy.api.repository;

import com.tripjoy.api.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface ConversationRepository extends JpaRepository<Conversation, UUID> {
    List<Conversation> findByGroup_Id(UUID groupId);

    @Query("SELECT c FROM Conversation c JOIN c.members m WHERE m.user.id = :userId")
    List<Conversation> findAllByUserId(@Param("userId") UUID userId);

    // === FILTER DELETED RECORDS ===

    @Query("SELECT c FROM Conversation c WHERE c.group.id = :groupId AND c.softDeleteInfo.isDeleted = false")
    List<Conversation> findByGroupIdAndNotDeleted(@Param("groupId") UUID groupId);

    @Query("SELECT c FROM Conversation c JOIN c.members m WHERE m.user.id = :userId AND c.softDeleteInfo.isDeleted = false")
    List<Conversation> findAllByUserIdAndNotDeleted(@Param("userId") UUID userId);

    // === SOFT DELETE CASCADE METHODS ===

    /**
     * Bulk soft delete all conversations of a group
     */
    @Modifying
    @Query("UPDATE Conversation c " +
            "SET c.softDeleteInfo.isDeleted = true, " +
            "    c.softDeleteInfo.deletedAt = :deletedAt, " +
            "    c.softDeleteInfo.deletedBy = :deletedBy " +
            "WHERE c.group.id = :groupId")
    int softDeleteByGroupId(@Param("groupId") UUID groupId,
            @Param("deletedAt") LocalDateTime deletedAt,
            @Param("deletedBy") String deletedBy);

    /**
     * Bulk restore all conversations of a group
     */
    @Modifying
    @Query("UPDATE Conversation c " +
            "SET c.softDeleteInfo.isDeleted = false, " +
            "    c.softDeleteInfo.deletedAt = null, " +
            "    c.softDeleteInfo.deletedBy = null " +
            "WHERE c.group.id = :groupId")
    int restoreByGroupId(@Param("groupId") UUID groupId);
}
