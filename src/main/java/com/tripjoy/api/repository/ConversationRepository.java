package com.tripjoy.api.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.tripjoy.api.entity.Conversation;

public interface ConversationRepository extends JpaRepository<Conversation, UUID> {

    List<Conversation> findByGroup_IdOrderByCreatedAtDesc(UUID groupId);

    /**
     * Find an existing DIRECT conversation between exactly two users.
     * Used for idempotency check before creating a new DM — if a DM already
     * exists between A and B, we return the existing one instead of creating a duplicate.
     *
     * <p>Logic: find a DIRECT conversation where both userA AND userB are members.
     */
    @Query("SELECT c FROM Conversation c "
            + "WHERE c.type = 'DIRECT' "
            + "AND EXISTS ("
            + "  SELECT 1 FROM ConversationMember cm1 "
            + "  WHERE cm1.conversation.id = c.id AND cm1.user.id = :userAId"
            + ") "
            + "AND EXISTS ("
            + "  SELECT 1 FROM ConversationMember cm2 "
            + "  WHERE cm2.conversation.id = c.id AND cm2.user.id = :userBId"
            + ")")
    Optional<Conversation> findDirectConversation(@Param("userAId") UUID userAId, @Param("userBId") UUID userBId);

    /**
     * Get all conversations for a user (Inbox), including both GROUP and DIRECT types.
     *
     * <p><b>GROUP:</b> Filtered by group soft-delete status and GroupMember active membership.
     * <p><b>DIRECT:</b> Always included — no group association to filter.
     *
     * <p>Ordered by last message timestamp DESC (newest first in inbox).
     */
    @Query("SELECT DISTINCT c FROM Conversation c "
            + "LEFT JOIN FETCH c.members m "
            + "LEFT JOIN FETCH m.user u "
            + "LEFT JOIN FETCH c.group g "
            + "WHERE EXISTS ("
            + "  SELECT 1 FROM ConversationMember cm "
            + "  WHERE cm.conversation.id = c.id "
            + "  AND cm.user.id = :userId"
            + ") "
            + "AND ("
            // DIRECT: no group association — always include
            + "  c.type = 'DIRECT' "
            + "  OR ("
            // GROUP: filter out soft-deleted groups and removed members
            + "    c.type = 'GROUP' "
            + "    AND g.softDeleteInfo.isDeleted = false "
            + "    AND EXISTS ("
            + "      SELECT 1 FROM GroupMember gm "
            + "      WHERE gm.group.id = g.id "
            + "      AND gm.user.id = :userId "
            + "      AND gm.softDeleteInfo.isDeleted = false"
            + "    )"
            + "  )"
            + ") "
            + "ORDER BY c.lastMessageTimestamp DESC NULLS LAST")
    List<Conversation> findAllByUserId(@Param("userId") UUID userId);
}
