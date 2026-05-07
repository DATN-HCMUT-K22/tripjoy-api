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

    @Query(value = """
            SELECT * FROM "groups" g
            WHERE lower(unaccent(g.name)) LIKE lower(unaccent(CONCAT('%', :keyword, '%')))
              AND g.is_deleted = false
            """, nativeQuery = true)
    List<Group> searchByName(@Param("keyword") String keyword);
}
