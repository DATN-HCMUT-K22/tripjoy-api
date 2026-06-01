package com.tripjoy.api.dto.projection;

/**
 * Internal read model for the admin dashboard overview query.
 *
 * <p>Keeping this projection separate from the API response allows the query implementation
 * to move to rollup tables later without changing the external contract.
 */
public interface AdminDashboardOverviewProjection {

    long getTotalUsers();

    long getLockedUsers();

    long getTotalPosts();

    long getTotalComments();

    long getTotalItineraries();

    long getTotalGroups();

    long getPendingReports();

    long getProcessedReports();

    long getDismissedReports();

    long getTotalModerationActions();
}
