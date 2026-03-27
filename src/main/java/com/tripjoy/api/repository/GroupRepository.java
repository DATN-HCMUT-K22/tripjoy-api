package com.tripjoy.api.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.tripjoy.api.entity.Group;

public interface GroupRepository extends JpaRepository<Group, UUID> {

    // === FILTER DELETED RECORDS ===

    @Query("SELECT g FROM Group g WHERE g.id = :id AND g.softDeleteInfo.isDeleted = false")
    Optional<Group> findByIdAndNotDeleted(@Param("id") UUID id);

    @Query("SELECT g FROM Group g WHERE g.softDeleteInfo.isDeleted = false")
    List<Group> findAllNotDeleted();

    @Query("SELECT g FROM Group g WHERE "
            + "LOWER(g.name) LIKE LOWER(CONCAT('%', :keyword, '%')) "
            + "AND g.softDeleteInfo.isDeleted = false")
    List<Group> searchByName(@Param("keyword") String keyword);
}
