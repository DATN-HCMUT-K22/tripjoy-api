package com.tripjoy.api.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tripjoy.api.entity.ReportContent;

@Repository
public interface ReportContentRepository extends JpaRepository<ReportContent, UUID> {

    @Override
    @EntityGraph(attributePaths = {"reporter", "reportedUser"})
    Page<ReportContent> findAll(Pageable pageable);
}
