package com.tripjoy.api.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Modifying;

import com.tripjoy.api.entity.ConversationMember;

public interface ConversationMemberRepository extends JpaRepository<ConversationMember, UUID> {

    // Check if member exists
    boolean existsByConversationIdAndUserId(UUID conversationId, UUID userId);

    // Find specific conversation member (no soft delete filter)
    @Query("SELECT cm FROM ConversationMember cm " + "WHERE cm.conversation.id = :conversationId "
            + "AND cm.user.id = :userId")
    Optional<ConversationMember> findByConversationIdAndUserId(
            @Param("conversationId") UUID conversationId, @Param("userId") UUID userId);

    // Delete specific member
    void deleteByConversationIdAndUserId(UUID conversationId, UUID userId);

    // Delete CASCADE handled by JPA orphanRemoval on Conversation

    @Modifying
    @Query("UPDATE ConversationMember cm SET cm.unreadCount = cm.unreadCount + 1 WHERE cm.conversation.id = :conversationId AND cm.user.id != :senderId")
    void incrementUnreadCountForOthers(@Param("conversationId") UUID conversationId, @Param("senderId") UUID senderId);

    @Modifying
    @Query("UPDATE ConversationMember cm SET cm.unreadCount = 0 WHERE cm.conversation.id = :conversationId AND cm.user.id = :userId")
    void resetUnreadCount(@Param("conversationId") UUID conversationId, @Param("userId") UUID userId);
}
