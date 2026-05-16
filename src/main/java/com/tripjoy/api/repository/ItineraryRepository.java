package com.tripjoy.api.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.tripjoy.api.entity.Itinerary;

public interface ItineraryRepository extends JpaRepository<Itinerary, UUID> {

    // === FILTER DELETED RECORDS ===

    @Query("SELECT i FROM Itinerary i WHERE i.group.id = :groupId AND i.softDeleteInfo.isDeleted = false")
    List<Itinerary> findByGroupIdAndNotDeleted(@Param("groupId") UUID groupId);

    @EntityGraph(attributePaths = {"user", "group", "origin", "destination", "travelNotebook", "themes"})
    @Query("SELECT DISTINCT i FROM Itinerary i " +
           "LEFT JOIN i.group g " +
           "LEFT JOIN g.members m " +
           "WHERE (i.user.id = :userId OR (m.user.id = :userId AND m.softDeleteInfo.isDeleted = false)) " +
           "AND i.softDeleteInfo.isDeleted = false")
    List<Itinerary> findByUserId(@Param("userId") UUID userId);

    @EntityGraph(attributePaths = {"user", "group", "origin", "destination", "travelNotebook", "themes"})
    @Query(
            "SELECT i FROM Itinerary i JOIN i.favouriteUsers f WHERE f.id = :userId AND i.softDeleteInfo.isDeleted = false")
    List<Itinerary> findByFavouriteUserId(@Param("userId") UUID userId);

    // === SOFT DELETE CASCADE METHODS ===

    /**
     * Bulk soft delete all itineraries of a group
     */
    @Modifying
    @Query("UPDATE Itinerary i " + "SET i.softDeleteInfo.isDeleted = true, "
            + "    i.softDeleteInfo.deletedAt = :deletedAt, "
            + "    i.softDeleteInfo.deletedBy = :deletedBy "
            + "WHERE i.group.id = :groupId")
    int softDeleteByGroupId(
            @Param("groupId") UUID groupId,
            @Param("deletedAt") LocalDateTime deletedAt,
            @Param("deletedBy") String deletedBy);

    /**
     * Bulk restore all itineraries of a group
     */
    @Modifying
    @Query("UPDATE Itinerary i " + "SET i.softDeleteInfo.isDeleted = false, "
            + "    i.softDeleteInfo.deletedAt = null, "
            + "    i.softDeleteInfo.deletedBy = null "
            + "WHERE i.group.id = :groupId")
    int restoreByGroupId(@Param("groupId") UUID groupId);
}
