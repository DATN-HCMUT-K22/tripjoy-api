package com.tripjoy.api.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tripjoy.api.entity.ModerationAction;

@Repository
public interface ModerationActionRepository extends JpaRepository<ModerationAction, UUID> {

    @Query("SELECT m FROM ModerationAction m WHERE " +
           "(cast(:q as text) IS NULL OR cast(m.user.id as text) = cast(:q as text) OR LOWER(m.user.username) LIKE LOWER(CONCAT('%', cast(:q as text), '%')) OR LOWER(m.user.email) LIKE LOWER(CONCAT('%', cast(:q as text), '%'))) AND " +
           "(cast(:actionType as text) IS NULL OR UPPER(m.actionType) = UPPER(cast(:actionType as text))) AND " +
           "(cast(:baId as uuid) IS NULL OR m.ba.id = :baId)")
    Page<ModerationAction> findByFilters(
            @Param("q") String q,
            @Param("actionType") String actionType,
            @Param("baId") UUID baId,
            Pageable pageable);
}

