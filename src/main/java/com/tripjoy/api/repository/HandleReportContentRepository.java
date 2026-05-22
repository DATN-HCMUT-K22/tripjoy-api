package com.tripjoy.api.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tripjoy.api.entity.HandleReportContent;
import com.tripjoy.api.entity.User;

@Repository
public interface HandleReportContentRepository extends JpaRepository<HandleReportContent, UUID> {

    /**
     * Find report with report content (JOIN FETCH to avoid N+1)
     */
    @Query("SELECT hrc FROM HandleReportContent hrc " +
           "JOIN FETCH hrc.reportContent " +
           "JOIN FETCH hrc.ba " +
           "WHERE hrc.id = :id")
    Optional<HandleReportContent> findByIdWithDetails(@Param("id") UUID id);

    /**
     * Find all reports with filters (status, type, date range)
     */
    @Query("SELECT hrc FROM HandleReportContent hrc " +
           "JOIN FETCH hrc.reportContent rc " +
           "JOIN FETCH hrc.ba " +
           "WHERE (:status IS NULL OR rc.status = :status) " +
           "AND (:reportType IS NULL OR hrc.report_type = :reportType) " +
           "AND (:startDate IS NULL OR hrc.createdAt >= :startDate) " +
           "AND (:endDate IS NULL OR hrc.createdAt <= :endDate)")
    Page<HandleReportContent> findWithFilters(
            @Param("status") String status,
            @Param("reportType") String reportType,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    /**
     * Count reports by report type
     */
    @Query("SELECT COUNT(hrc) FROM HandleReportContent hrc WHERE hrc.report_type = :reportType")
    long countByReportType(@Param("reportType") String reportType);

    /**
     * Find top reporters (users who submitted most reports)
     */
    @Query("SELECT hrc.ba as user, COUNT(hrc) as reportCount " +
           "FROM HandleReportContent hrc " +
           "GROUP BY hrc.ba " +
           "ORDER BY reportCount DESC")
    List<Object[]> findTopReporters(Pageable pageable);

    /**
     * Find all reports submitted by a specific user
     */
    Page<HandleReportContent> findByBa(User reporter, Pageable pageable);
}
