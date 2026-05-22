package com.tripjoy.api.repository;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tripjoy.api.entity.ReportContent;

@Repository
public interface ReportContentRepository extends JpaRepository<ReportContent, UUID> {

    /**
     * Find all reports by status with pagination
     */
    Page<ReportContent> findByStatus(String status, Pageable pageable);

    /**
     * Find all reports by content type with pagination
     */
    Page<ReportContent> findByContentType(String contentType, Pageable pageable);

    /**
     * Find all reports by status and content type
     */
    Page<ReportContent> findByStatusAndContentType(
            String status, String contentType, Pageable pageable);

    /**
     * Find reports created between dates
     */
    @Query("SELECT rc FROM ReportContent rc WHERE rc.createdAt BETWEEN :startDate AND :endDate")
    Page<ReportContent> findByCreatedAtBetween(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    /**
     * Count reports by status
     */
    long countByStatus(String status);

    /**
     * Count reports by content type
     */
    long countByContentType(String contentType);

    /**
     * Count all reports created after a certain date
     */
    @Query("SELECT COUNT(rc) FROM ReportContent rc WHERE rc.createdAt >= :date")
    long countCreatedAfter(@Param("date") LocalDateTime date);
}
