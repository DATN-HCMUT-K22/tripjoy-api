package com.tripjoy.api.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tripjoy.api.entity.TravelNotebook;

@Repository
public interface TravelNotebookRepository extends JpaRepository<TravelNotebook, UUID> {

    /** Tìm notebook theo itinerary_id (quan hệ @OneToOne) */
    Optional<TravelNotebook> findByItineraryId(UUID itineraryId);

    /** Kiểm tra có notebook cho itinerary này chưa */
    boolean existsByItineraryId(UUID itineraryId);
}
