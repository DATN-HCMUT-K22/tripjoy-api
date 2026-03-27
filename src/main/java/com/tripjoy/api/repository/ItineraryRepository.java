package com.tripjoy.api.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.tripjoy.api.entity.Itinerary;

public interface ItineraryRepository extends JpaRepository<Itinerary, UUID> {

    // === FILTER DELETED RECORDS ===

    @Query("SELECT i FROM Itinerary i WHERE i.group.id = :groupId AND i.softDeleteInfo.isDeleted = false")
    List<Itinerary> findByGroupIdAndNotDeleted(@Param("groupId") UUID groupId);

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
