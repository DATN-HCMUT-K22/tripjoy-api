package com.tripjoy.api.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tripjoy.api.entity.Notification;
import com.tripjoy.api.enums.NotificationType;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    // GET USER'S NOTIFICATIONS

    /**
     * Get all notifications for a user
     * Optimized with composite index: idx_recipient_created
     */
    @Query("SELECT n FROM Notification n " + "WHERE n.recipient.id = :userId " + "ORDER BY n.createdAt DESC")
    Page<Notification> findByRecipient(@Param("userId") UUID userId, Pageable pageable);

    /**
     * Get unread notifications only
     * Optimized with composite index: idx_recipient_unread
     */
    @Query("SELECT n FROM Notification n " + "WHERE n.recipient.id = :userId "
            + "AND n.isRead = false "
            + "ORDER BY n.createdAt DESC")
    Page<Notification> findUnreadByRecipient(@Param("userId") UUID userId, Pageable pageable);

    /**
     * Get archived notifications
     */
    @Query("SELECT n FROM Notification n " + "WHERE n.recipient.id = :userId "
            + "AND n.isArchived = true "
            + "ORDER BY n.createdAt DESC")
    Page<Notification> findArchivedByRecipient(@Param("userId") UUID userId, Pageable pageable);

    /**
     * Get notifications by type for a user
     */
    @Query("SELECT n FROM Notification n " + "WHERE n.recipient.id = :userId "
            + "AND n.type = :type "
            + "ORDER BY n.createdAt DESC")
    Page<Notification> findByRecipientAndType(
            @Param("userId") UUID userId, @Param("type") NotificationType type, Pageable pageable);

    // COUNT QUERIES

    /**
     * Count unread notifications for a user
     * Fast query with composite index: idx_recipient_unread
     */
    @Query("SELECT COUNT(n) FROM Notification n " + "WHERE n.recipient.id = :userId " + "AND n.isRead = false")
    Long countUnreadByRecipient(@Param("userId") UUID userId);

    /**
     * Count total notifications for a user
     */
    @Query("SELECT COUNT(n) FROM Notification n " + "WHERE n.recipient.id = :userId")
    Long countByRecipient(@Param("userId") UUID userId);

    // FIND BY ID WITH VALIDATION

    /**
     * Find notification by ID and recipient (security check)
     */
    @Query("SELECT n FROM Notification n " + "WHERE n.id = :notificationId " + "AND n.recipient.id = :userId")
    Optional<Notification> findByIdAndRecipient(
            @Param("notificationId") UUID notificationId, @Param("userId") UUID userId);

    // UPDATE OPERATIONS

    /**
     * Mark notification as read
     */
    @Modifying
    @Query("UPDATE Notification n " + "SET n.isRead = true, "
            + "    n.readAt = :readAt, "
            + "    n.updatedAt = :readAt "
            + "WHERE n.id = :notificationId "
            + "AND n.recipient.id = :userId")
    int markAsRead(
            @Param("notificationId") UUID notificationId,
            @Param("userId") UUID userId,
            @Param("readAt") LocalDateTime readAt);

    /**
     * Mark all notifications as read for a user
     */
    @Modifying
    @Query("UPDATE Notification n " + "SET n.isRead = true, "
            + "    n.readAt = :readAt, "
            + "    n.updatedAt = :readAt "
            + "WHERE n.recipient.id = :userId "
            + "AND n.isRead = false")
    int markAllAsRead(@Param("userId") UUID userId, @Param("readAt") LocalDateTime readAt);

    /**
     * Archive notification
     */
    @Modifying
    @Query("UPDATE Notification n " + "SET n.isArchived = :archived, "
            + "    n.updatedAt = :now "
            + "WHERE n.id = :notificationId "
            + "AND n.recipient.id = :userId")
    int updateArchived(
            @Param("notificationId") UUID notificationId,
            @Param("userId") UUID userId,
            @Param("archived") boolean archived,
            @Param("now") LocalDateTime now);

    // ENTITY QUERIES (for deduplication/cleanup)

    /**
     * Find notifications by entity reference
     * Optimized with index: idx_entity
     */
    @Query("SELECT n FROM Notification n " + "WHERE n.entityType = :entityType " + "AND n.entityId = :entityId")
    List<Notification> findByEntity(@Param("entityType") String entityType, @Param("entityId") String entityId);

    /**
     * Find notifications triggered by a specific user (rare use case)
     */
    @Query("SELECT n FROM Notification n " + "WHERE n.actor.id = :actorId " + "ORDER BY n.createdAt DESC")
    Page<Notification> findByActor(@Param("actorId") UUID actorId, Pageable pageable);

    // CLEANUP QUERIES

    /**
     * Archive old read notifications (older than X days)
     */
    @Modifying
    @Query("UPDATE Notification n " + "SET n.isArchived = true, "
            + "    n.updatedAt = :now "
            + "WHERE n.isRead = true "
            + "AND n.isArchived = false "
            + "AND n.createdAt < :threshold")
    int archiveOldReadNotifications(@Param("threshold") LocalDateTime threshold, @Param("now") LocalDateTime now);
}
