package com.tripjoy.api.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tripjoy.api.entity.ActivityLog;
import com.tripjoy.api.enums.ActivityAction;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, UUID> {

    // GET USER ACTIVITY LOGS

    /**
     * Get all activity logs for a user (paginated)
     */
    @Query("SELECT a FROM ActivityLog a " + "WHERE a.user.id = :userId " + "ORDER BY a.createdAt DESC")
    Page<ActivityLog> findByUserId(@Param("userId") UUID userId, Pageable pageable);

    /**
     * Get activity logs by action type
     */
    @Query("SELECT a FROM ActivityLog a " + "WHERE a.action = :action " + "ORDER BY a.createdAt DESC")
    Page<ActivityLog> findByAction(@Param("action") ActivityAction action, Pageable pageable);

    /**
     * Get activity logs for specific user and action
     */
    @Query("SELECT a FROM ActivityLog a " + "WHERE a.user.id = :userId "
            + "AND a.action = :action "
            + "ORDER BY a.createdAt DESC")
    Page<ActivityLog> findByUserIdAndAction(
            @Param("userId") UUID userId, @Param("action") ActivityAction action, Pageable pageable);

    // ENTITY AUDIT TRAIL

    /**
     * Get audit trail for specific entity
     */
    @Query("SELECT a FROM ActivityLog a " + "WHERE a.entityType = :entityType "
            + "AND a.entityId = :entityId "
            + "ORDER BY a.createdAt DESC")
    List<ActivityLog> findByEntity(@Param("entityType") String entityType, @Param("entityId") String entityId);

    /**
     * Get recent activities (for admin dashboard)
     */
    @Query("SELECT a FROM ActivityLog a " + "ORDER BY a.createdAt DESC")
    Page<ActivityLog> findRecentActivities(Pageable pageable);

    /**
     * Count activities by user
     */
    @Query("SELECT COUNT(a) FROM ActivityLog a " + "WHERE a.user.id = :userId")
    Long countByUserId(@Param("userId") UUID userId);

    /**
     * Count activities by action
     */
    @Query("SELECT COUNT(a) FROM ActivityLog a " + "WHERE a.action = :action")
    Long countByAction(@Param("action") ActivityAction action);

    // CLEANUP

    /**
     * Delete old activity logs (cleanup job)
     * Typically run monthly to keep table size manageable
     */
    @Modifying
    @Query("DELETE FROM ActivityLog a " + "WHERE a.createdAt < :threshold")
    int deleteOldLogs(@Param("threshold") LocalDateTime threshold);

    /**
     * Get activity logs in date range (for reporting)
     */
    @Query("SELECT a FROM ActivityLog a " + "WHERE a.createdAt BETWEEN :startDate AND :endDate "
            + "ORDER BY a.createdAt DESC")
    List<ActivityLog> findByDateRange(
            @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}
