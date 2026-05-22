package com.tripjoy.api.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tripjoy.api.entity.ModerationAction;
import com.tripjoy.api.entity.User;

@Repository
public interface ModerationActionRepository extends JpaRepository<ModerationAction, UUID> {

    /**
     * Find all moderation actions for a specific user (chronologically ordered)
     */
    @Query("SELECT ma FROM ModerationAction ma " +
           "JOIN FETCH ma.user " +
           "JOIN FETCH ma.ba " +
           "WHERE ma.user.id = :userId " +
           "ORDER BY ma.createdAt ASC")
    List<ModerationAction> findByUserIdOrderByCreatedAtAsc(@Param("userId") UUID userId);

    /**
     * Find moderation actions with filters (user, action type, date range)
     */
    @Query("SELECT ma FROM ModerationAction ma " +
           "JOIN FETCH ma.user " +
           "JOIN FETCH ma.ba " +
           "WHERE (:userId IS NULL OR ma.user.id = :userId) " +
           "AND (:actionType IS NULL OR ma.actionType = :actionType) " +
           "AND (:startDate IS NULL OR ma.createdAt >= :startDate) " +
           "AND (:endDate IS NULL OR ma.createdAt <= :endDate)")
    Page<ModerationAction> findWithFilters(
            @Param("userId") UUID userId,
            @Param("actionType") String actionType,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    /**
     * Count actions by action type
     */
    long countByActionType(String actionType);

    /**
     * Find most recent moderation action for a user
     */
    Optional<ModerationAction> findFirstByUserOrderByCreatedAtDesc(User user);

    /**
     * Count moderation actions created after a certain date
     */
    @Query("SELECT COUNT(ma) FROM ModerationAction ma WHERE ma.createdAt >= :date")
    long countCreatedAfter(@Param("date") LocalDateTime date);
}
