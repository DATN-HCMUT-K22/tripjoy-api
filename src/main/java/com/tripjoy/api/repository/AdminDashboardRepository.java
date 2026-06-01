package com.tripjoy.api.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import com.tripjoy.api.dto.projection.AdminDashboardOverviewProjection;
import com.tripjoy.api.entity.User;

/**
 * Read-only aggregate queries for the admin dashboard.
 *
 * <p>The overview query intentionally uses one database round-trip. It can later be redirected
 * to rollup tables without changing the service or API contract.
 */
public interface AdminDashboardRepository extends Repository<User, UUID> {

    @Query(
            value =
                    """
					SELECT
						(SELECT COUNT(*) FROM users WHERE is_deleted = FALSE) AS "totalUsers",
						(SELECT COUNT(*) FROM users WHERE is_deleted = FALSE AND is_locked = TRUE) AS "lockedUsers",
						(SELECT COUNT(*) FROM post WHERE is_deleted = FALSE) AS "totalPosts",
						(SELECT COUNT(*) FROM comment WHERE is_deleted IS NOT TRUE) AS "totalComments",
						(SELECT COUNT(*) FROM itinerary WHERE is_deleted = FALSE) AS "totalItineraries",
						(SELECT COUNT(*) FROM groups WHERE is_deleted = FALSE) AS "totalGroups",
						(SELECT COUNT(*) FROM report_content WHERE status = 'PENDING') AS "pendingReports",
						(SELECT COUNT(*) FROM report_content WHERE status = 'PROCESSED') AS "processedReports",
						(SELECT COUNT(*) FROM report_content WHERE status = 'DISMISSED') AS "dismissedReports",
						(SELECT COUNT(*) FROM moderation_action) AS "totalModerationActions"
					""",
            nativeQuery = true)
    AdminDashboardOverviewProjection getOverview();
}
