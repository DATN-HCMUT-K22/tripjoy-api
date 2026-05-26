package com.tripjoy.api.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tripjoy.api.entity.Feedback;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, UUID> {

    @Override
    @EntityGraph(
            attributePaths = {
                "sender",
                "receiver",
                "parentFeedback",
                "reportContent",
                "reportContent.reporter",
                "reportContent.reportedUser"
            })
    Page<Feedback> findAll(Pageable pageable);

    @Override
    @EntityGraph(
            attributePaths = {
                "sender",
                "receiver",
                "parentFeedback",
                "reportContent",
                "reportContent.reporter",
                "reportContent.reportedUser"
            })
    Optional<Feedback> findById(UUID id);
}
