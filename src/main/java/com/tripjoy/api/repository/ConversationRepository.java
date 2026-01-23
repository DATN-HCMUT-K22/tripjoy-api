package com.tripjoy.api.repository;

import com.tripjoy.api.entity.Conversation;
import com.tripjoy.api.entity.GroupMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ConversationRepository extends JpaRepository<Conversation, UUID> {

        // Get conversations by group (no soft delete filter needed - conversations hard
        // deleted)
        List<Conversation> findByGroup_Id(UUID groupId);

        // Get user's conversations (filter Group AND GroupMember soft delete)
        // This query ensures:
        // 1. User is a conversation member
        // 2. Group is not soft deleted
        // 3. User's GroupMember is not soft deleted (critical for MEMBER role!)
        @Query("SELECT DISTINCT c FROM Conversation c " +
                        "JOIN c.members m " +
                        "JOIN GroupMember gm ON gm.group.id = c.group.id AND gm.user.id = m.user.id " +
                        "WHERE m.user.id = :userId " +
                        "AND c.group.softDeleteInfo.isDeleted = false " +
                        "AND gm.softDeleteInfo.isDeleted = false")
        List<Conversation> findAllByUserId(@Param("userId") UUID userId);

        // Hard delete CASCADE handled automatically by JPA orphanRemoval
        // No manual delete methods needed
}
