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
           "(cast(:userId as uuid) IS NULL OR m.user.id = :userId) AND " +
           "(cast(:actionType as text) IS NULL OR UPPER(m.actionType) = UPPER(cast(:actionType as text))) AND " +
           "(cast(:baId as uuid) IS NULL OR m.ba.id = :baId)")
    Page<ModerationAction> findByFilters(
            @Param("userId") UUID userId,
            @Param("actionType") String actionType,
            @Param("baId") UUID baId,
            Pageable pageable);
}

