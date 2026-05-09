package com.tripjoy.api.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tripjoy.api.entity.ChatMessage;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, UUID> {

    // Count pinned messages in a conversation (no soft delete filter)
    @Query("SELECT COUNT(cm) FROM ChatMessage cm " + "WHERE cm.conversation.id = :conversationId "
            + "AND cm.isPinned = true")
    long countPinnedByConversationId(@Param("conversationId") UUID conversationId);

    // Get all pinned messages in a conversation (ordered by createdAt DESC)
    @Query("SELECT cm FROM ChatMessage cm " + "LEFT JOIN FETCH cm.sharedPost sp " + "LEFT JOIN FETCH sp.creator "
            + "WHERE cm.conversation.id = :conversationId "
            + "AND cm.isPinned = true "
            + "ORDER BY cm.createdAt DESC")
    List<ChatMessage> findPinnedByConversationId(@Param("conversationId") UUID conversationId);

    // Cursor-based pagination: Initial load - Get latest N messages
    @Query("SELECT DISTINCT cm FROM ChatMessage cm " + "LEFT JOIN FETCH cm.likeUsers "
            + "LEFT JOIN FETCH cm.sharedPost sp " + "LEFT JOIN FETCH sp.creator "
            + "WHERE cm.conversation.id = :conversationId "
            + "ORDER BY cm.createdAt DESC")
    List<ChatMessage> findLatestMessages(@Param("conversationId") UUID conversationId, Pageable pageable);

    // Cursor-based pagination: Load older messages (scroll up) - before cursor
    @Query("SELECT DISTINCT cm FROM ChatMessage cm " + "LEFT JOIN FETCH cm.likeUsers "
            + "LEFT JOIN FETCH cm.sharedPost sp " + "LEFT JOIN FETCH sp.creator "
            + "WHERE cm.conversation.id = :conversationId "
            + "AND cm.createdAt < :before "
            + "ORDER BY cm.createdAt DESC")
    List<ChatMessage> findMessagesBefore(
            @Param("conversationId") UUID conversationId, @Param("before") LocalDateTime before, Pageable pageable);

    // Cursor-based pagination: Load newer messages - after cursor
    @Query("SELECT DISTINCT cm FROM ChatMessage cm " + "LEFT JOIN FETCH cm.likeUsers "
            + "LEFT JOIN FETCH cm.sharedPost sp " + "LEFT JOIN FETCH sp.creator "
            + "WHERE cm.conversation.id = :conversationId "
            + "AND cm.createdAt > :after "
            + "ORDER BY cm.createdAt ASC") // ASC for newer messages
    List<ChatMessage> findMessagesAfter(
            @Param("conversationId") UUID conversationId, @Param("after") LocalDateTime after, Pageable pageable);

    // Delete CASCADE handled by JPA orphanRemoval on Conversation

    /**
     * Full-Text Search messages within a conversation.
     * Uses PostgreSQL to_tsvector/plainto_tsquery with GIN index.
     * Falls back to ILIKE for partial matches (e.g. very short keywords).
     * Results sorted by relevance (ts_rank) DESC, then created_at DESC.
     */
    @Query(
            value =
                    """
						SELECT cm.* FROM chat_message cm
						WHERE cm.conversation_id = :conversationId
						AND (
							to_tsvector('simple', f_unaccent(coalesce(cm.message_content, '')))
								@@ plainto_tsquery('simple', f_unaccent(:keyword))
							OR f_unaccent(cm.message_content) ILIKE '%' || f_unaccent(:keyword) || '%'
						)
						ORDER BY
							ts_rank(to_tsvector('simple', f_unaccent(coalesce(cm.message_content, ''))),
									plainto_tsquery('simple', f_unaccent(:keyword))) DESC,
							cm.created_at DESC
						LIMIT :limit OFFSET :offset
						""",
            nativeQuery = true)
    List<ChatMessage> searchMessages(
            @Param("conversationId") UUID conversationId,
            @Param("keyword") String keyword,
            @Param("limit") int limit,
            @Param("offset") int offset);

    /**
     * Count total search results (for pagination metadata).
     */
    @Query(
            value =
                    """
						SELECT COUNT(*) FROM chat_message cm
						WHERE cm.conversation_id = :conversationId
						AND (
							to_tsvector('simple', f_unaccent(coalesce(cm.message_content, '')))
								@@ plainto_tsquery('simple', f_unaccent(:keyword))
							OR f_unaccent(cm.message_content) ILIKE '%' || f_unaccent(:keyword) || '%'
						)
						""",
            nativeQuery = true)
    long countSearchMessages(@Param("conversationId") UUID conversationId, @Param("keyword") String keyword);

    /**
     * Global Full-Text Search across ALL conversations the user belongs to.
     * Filters by conversation_member to ensure user can only search their own
     * conversations.
     */
    @Query(
            value =
                    """
						SELECT cm.* FROM chat_message cm
						WHERE cm.conversation_id IN (
							SELECT cmb.conversation_id FROM conversation_member cmb
							WHERE cmb.user_id = :userId
						)
						AND (
							to_tsvector('simple', f_unaccent(coalesce(cm.message_content, '')))
								@@ plainto_tsquery('simple', f_unaccent(:keyword))
							OR f_unaccent(cm.message_content) ILIKE '%' || f_unaccent(:keyword) || '%'
						)
						ORDER BY
							ts_rank(to_tsvector('simple', f_unaccent(coalesce(cm.message_content, ''))),
									plainto_tsquery('simple', f_unaccent(:keyword))) DESC,
							cm.created_at DESC
						LIMIT :limit OFFSET :offset
						""",
            nativeQuery = true)
    List<ChatMessage> searchMessagesGlobal(
            @Param("userId") UUID userId,
            @Param("keyword") String keyword,
            @Param("limit") int limit,
            @Param("offset") int offset);

    /**
     * Count total global search results.
     */
    @Query(
            value =
                    """
						SELECT COUNT(*) FROM chat_message cm
						WHERE cm.conversation_id IN (
							SELECT cmb.conversation_id FROM conversation_member cmb
							WHERE cmb.user_id = :userId
						)
						AND (
							to_tsvector('simple', f_unaccent(coalesce(cm.message_content, '')))
								@@ plainto_tsquery('simple', f_unaccent(:keyword))
							OR f_unaccent(cm.message_content) ILIKE '%' || f_unaccent(:keyword) || '%'
						)
						""",
            nativeQuery = true)
    long countSearchMessagesGlobal(@Param("userId") UUID userId, @Param("keyword") String keyword);
}
