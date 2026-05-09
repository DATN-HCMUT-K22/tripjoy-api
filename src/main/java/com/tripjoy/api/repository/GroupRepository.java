package com.tripjoy.api.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
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

    @EntityGraph(attributePaths = {"members"})
    @Query(
            "SELECT g FROM Group g WHERE CAST(function('lower', function('f_unaccent', g.name)) AS string) LIKE CAST(function('lower', function('f_unaccent', CONCAT('%', :keyword, '%'))) AS string) AND g.softDeleteInfo.isDeleted = false")
    List<Group> searchByName(@Param("keyword") String keyword);
}
