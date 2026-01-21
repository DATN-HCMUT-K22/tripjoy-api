package com.tripjoy.api.repository;

import com.tripjoy.api.entity.ChatMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, UUID> {

        // Count pinned messages in a conversation
        @Query("SELECT COUNT(cm) FROM ChatMessage cm WHERE cm.conversation.id = :conversationId AND cm.isPinned = true AND cm.softDeleteInfo.isDeleted = false")
        long countPinnedByConversationId(@Param("conversationId") UUID conversationId);

        // Get all pinned messages in a conversation (ordered by createdAt DESC)
        @Query("SELECT cm FROM ChatMessage cm WHERE cm.conversation.id = :conversationId AND cm.isPinned = true AND cm.softDeleteInfo.isDeleted = false ORDER BY cm.createdAt DESC")
        List<ChatMessage> findPinnedByConversationId(@Param("conversationId") UUID conversationId);

        // Cursor-based pagination: Initial load - Get latest N messages
        @Query("SELECT DISTINCT cm FROM ChatMessage cm " +
                        "LEFT JOIN FETCH cm.likeUsers " +
                        "WHERE cm.conversation.id = :conversationId " +
                        "AND cm.softDeleteInfo.isDeleted = false " +
                        "ORDER BY cm.createdAt DESC")
        List<ChatMessage> findLatestMessages(
                        @Param("conversationId") UUID conversationId,
                        Pageable pageable);

        // Cursor-based pagination: Load older messages (scroll up) - before cursor
        @Query("SELECT DISTINCT cm FROM ChatMessage cm " +
                        "LEFT JOIN FETCH cm.likeUsers " +
                        "WHERE cm.conversation.id = :conversationId " +
                        "AND cm.createdAt < :before " +
                        "AND cm.softDeleteInfo.isDeleted = false " +
                        "ORDER BY cm.createdAt DESC")
        List<ChatMessage> findMessagesBefore(
                        @Param("conversationId") UUID conversationId,
                        @Param("before") LocalDateTime before,
                        Pageable pageable);

        // Cursor-based pagination: Load newer messages - after cursor
        @Query("SELECT DISTINCT cm FROM ChatMessage cm " +
                        "LEFT JOIN FETCH cm.likeUsers " +
                        "WHERE cm.conversation.id = :conversationId " +
                        "AND cm.createdAt > :after " +
                        "AND cm.softDeleteInfo.isDeleted = false " +
                        "ORDER BY cm.createdAt ASC") // ASC for newer messages
        List<ChatMessage> findMessagesAfter(
                        @Param("conversationId") UUID conversationId,
                        @Param("after") LocalDateTime after,
                        Pageable pageable);
}
