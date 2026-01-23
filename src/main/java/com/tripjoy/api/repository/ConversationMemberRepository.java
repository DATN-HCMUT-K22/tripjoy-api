package com.tripjoy.api.repository;

import com.tripjoy.api.entity.ConversationMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ConversationMemberRepository extends JpaRepository<ConversationMember, UUID> {

        // Check if member exists
        boolean existsByConversationIdAndUserId(UUID conversationId, UUID userId);

        // Find specific conversation member (no soft delete filter)
        @Query("SELECT cm FROM ConversationMember cm " +
                        "WHERE cm.conversation.id = :conversationId " +
                        "AND cm.user.id = :userId")
        Optional<ConversationMember> findByConversationIdAndUserId(
                        @Param("conversationId") UUID conversationId,
                        @Param("userId") UUID userId);

        // Delete specific member
        void deleteByConversationIdAndUserId(UUID conversationId, UUID userId);

        // Delete CASCADE handled by JPA orphanRemoval on Conversation
}
