package com.tripjoy.api.repository;

import com.tripjoy.api.entity.Conversation;
import com.tripjoy.api.entity.ConversationMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConversationMemberRepository extends JpaRepository<ConversationMember, UUID> {
        boolean existsByConversationIdAndUserId(UUID conversationId, UUID userId);

        // Delete conversation member when removing from group
        void deleteByConversationIdAndUserId(UUID conversationId, UUID userId);

        // Find member by conversation and user
        @Query("SELECT cm FROM ConversationMember cm WHERE cm.conversation.id = :conversationId AND cm.user.id = :userId AND cm.softDeleteInfo.isDeleted = false")
        Optional<ConversationMember> findByConversationIdAndUserId(@Param("conversationId") UUID conversationId,
                        @Param("userId") UUID userId);

        // === FILTER DELETED RECORDS ===

        @Query("SELECT cm FROM ConversationMember cm WHERE cm.conversation.id = :conversationId AND cm.user.id = :userId AND cm.softDeleteInfo.isDeleted = false")
        boolean existsByConversationIdAndUserIdAndNotDeleted(@Param("conversationId") UUID conversationId,
                        @Param("userId") UUID userId);

        @Query("SELECT cm FROM ConversationMember cm WHERE cm.conversation.id = :conversationId AND cm.softDeleteInfo.isDeleted = false")
        List<ConversationMember> findByConversationIdAndNotDeleted(@Param("conversationId") UUID conversationId);

        // === SOFT DELETE CASCADE METHODS ===

        /**
         * Bulk soft delete all conversation members of group conversations
         */
        @Modifying
        @Query("UPDATE ConversationMember cm " +
                        "SET cm.softDeleteInfo.isDeleted = true, " +
                        "    cm.softDeleteInfo.deletedAt = :deletedAt, " +
                        "    cm.softDeleteInfo.deletedBy = :deletedBy " +
                        "WHERE cm.conversation.id IN " +
                        "(SELECT c.id FROM Conversation c WHERE c.group.id = :groupId)")
        int softDeleteByGroupConversations(@Param("groupId") UUID groupId,
                        @Param("deletedAt") LocalDateTime deletedAt,
                        @Param("deletedBy") String deletedBy);

        /**
         * Bulk restore all conversation members of group conversations
         */
        @Modifying
        @Query("UPDATE ConversationMember cm " +
                        "SET cm.softDeleteInfo.isDeleted = false, " +
                        "    cm.softDeleteInfo.deletedAt = null, " +
                        "    cm.softDeleteInfo.deletedBy = null " +
                        "WHERE cm.conversation.id IN " +
                        "(SELECT c.id FROM Conversation c WHERE c.group.id = :groupId)")
        int restoreByGroupConversations(@Param("groupId") UUID groupId);
}
