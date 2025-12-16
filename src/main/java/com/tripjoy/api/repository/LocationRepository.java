package com.tripjoy.api.repository;

import com.tripjoy.api.entity.Location;
import org.locationtech.jts.geom.Point;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LocationRepository extends JpaRepository<Location, UUID> {

  Optional<Location> findByProviderId(String providerId);

  @Query(value = """
        SELECT * FROM location
        WHERE ST_DWithin(
            coordinates::geography,
            CAST(:point AS geography),
            50
        )
        AND deleted_at IS NULL
        LIMIT 1
        """, nativeQuery = true)
  List<Location> findWithin50Meters(@Param("point") Point point);

  @Query(value = """
        SELECT * FROM location
        WHERE ST_DWithin(
            coordinates::geography,
            CAST(:point AS geography),
            :meters
        )
        AND deleted_at IS NULL
        """, nativeQuery = true)
  List<Location> findWithinDistance(
          @Param("point") Point point,
          @Param("meters") double meters);

  @Query("SELECT COUNT(sl) FROM SuggestLocation sl WHERE sl.location.id = :locationId")
  Long countSuggestLocationsByLocationId(@Param("locationId") UUID locationId);

  @Query(value = """
        SELECT * FROM location l
        WHERE (:query IS NULL OR 
               LOWER(l.name) LIKE LOWER(CONCAT('%', :query, '%')) OR 
               LOWER(l.full_address) LIKE LOWER(CONCAT('%', :query, '%')))
          AND (:city IS NULL OR 
               LOWER(l.address_components ->> 'city') = LOWER(:city))
          AND (:district IS NULL OR 
               LOWER(l.address_components ->> 'district') = LOWER(:district))
          AND l.deleted_at IS NULL
        ORDER BY l.created_at DESC
        """,
          countQuery = """
        SELECT count(*) FROM location l
        WHERE (:query IS NULL OR 
               LOWER(l.name) LIKE LOWER(CONCAT('%', :query, '%')) OR 
               LOWER(l.full_address) LIKE LOWER(CONCAT('%', :query, '%')))
          AND (:city IS NULL OR 
               LOWER(l.address_components ->> 'city') = LOWER(:city))
          AND (:district IS NULL OR 
               LOWER(l.address_components ->> 'district') = LOWER(:district))
          AND l.deleted_at IS NULL
        """,
          nativeQuery = true)
  Page<Location> searchLocations(
          @Param("query") String query,
          @Param("city") String city,
          @Param("district") String district,
          Pageable pageable);
}