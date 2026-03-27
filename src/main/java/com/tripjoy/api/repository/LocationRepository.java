package com.tripjoy.api.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.locationtech.jts.geom.Point;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tripjoy.api.entity.Location;

@Repository
public interface LocationRepository extends JpaRepository<Location, UUID> {

    Optional<Location> findByProviderId(String providerId);

    List<Location> findByProviderIdIn(java.util.Collection<String> providerIds);

    @Query(
            value =
                    """
			SELECT * FROM location
			WHERE ST_DWithin(
				coordinates::geography,
				CAST(:point AS geography),
				50
			)
			LIMIT 1
			""",
            nativeQuery = true)
    List<Location> findWithin50Meters(@Param("point") Point point);

    @Query(
            value =
                    """
			SELECT * FROM location
			WHERE ST_DWithin(
				coordinates::geography,
				CAST(:point AS geography),
				:meters
			)
			""",
            nativeQuery = true)
    List<Location> findWithinDistance(@Param("point") Point point, @Param("meters") double meters);

    @Query("SELECT COUNT(sl) FROM SuggestLocation sl WHERE sl.location.id = :locationId")
    Long countSuggestLocationsByLocationId(@Param("locationId") UUID locationId);

    // use sql native instead of jpql to avoid lower() function applied to all columns :((((
    @Query(
            value =
                    """
			SELECT * FROM location l
			WHERE (:query IS NULL OR
					LOWER(l.name) LIKE LOWER(CONCAT('%', :query, '%')) OR
					LOWER(l.full_address) LIKE LOWER(CONCAT('%', :query, '%')))
				AND (:city IS NULL OR LOWER(l.city) = LOWER(:city))
				AND (:district IS NULL OR LOWER(l.district) = LOWER(:district))
			""",
            countQuery =
                    """
			SELECT count(*) FROM location l
			WHERE (:query IS NULL OR
					LOWER(l.name) LIKE LOWER(CONCAT('%', :query, '%')) OR
					LOWER(l.full_address) LIKE LOWER(CONCAT('%', :query, '%')))
				AND (:city IS NULL OR LOWER(l.city) = LOWER(:city))
				AND (:district IS NULL OR LOWER(l.district) = LOWER(:district))
			""",
            nativeQuery = true)
    Page<Location> searchLocations(
            @Param("query") String query,
            @Param("city") String city,
            @Param("district") String district,
            Pageable pageable);
}
