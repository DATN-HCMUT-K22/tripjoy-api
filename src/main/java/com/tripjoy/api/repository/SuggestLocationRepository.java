package com.tripjoy.api.repository;

import com.tripjoy.api.entity.SuggestLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SuggestLocationRepository extends JpaRepository<SuggestLocation, UUID> {

    List<SuggestLocation> findByGroupIdOrderByCreatedAtDesc(UUID groupId);

    boolean existsByIdAndSuggestedById(UUID id, UUID suggestedById);

    Optional<SuggestLocation> findByIdAndGroupId(UUID id, UUID groupId);
}
