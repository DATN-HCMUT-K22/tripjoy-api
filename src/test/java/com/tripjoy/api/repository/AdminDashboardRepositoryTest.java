package com.tripjoy.api.repository;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import com.tripjoy.api.dto.projection.AdminDashboardOverviewProjection;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AdminDashboardRepositoryTest {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    AdminDashboardRepository adminDashboardRepository;

    @BeforeEach
    void setUp() {
        UUID activeUserId = UUID.randomUUID();
        UUID lockedUserId = UUID.randomUUID();
        UUID deletedUserId = UUID.randomUUID();
        UUID postId = UUID.randomUUID();

        jdbcTemplate.update(
                "INSERT INTO users (id, is_email_verified, is_locked, is_deleted) VALUES (?, FALSE, FALSE, FALSE)",
                activeUserId);
        jdbcTemplate.update(
                "INSERT INTO users (id, is_email_verified, is_locked, is_deleted) VALUES (?, FALSE, TRUE, FALSE)",
                lockedUserId);
        jdbcTemplate.update(
                "INSERT INTO users (id, is_email_verified, is_locked, is_deleted) VALUES (?, FALSE, TRUE, TRUE)",
                deletedUserId);

        jdbcTemplate.update(
                "INSERT INTO post (id, visibility, hide_expense, is_deleted) VALUES (?, 'PUBLIC', FALSE, FALSE)",
                postId);
        jdbcTemplate.update(
                "INSERT INTO post (id, visibility, hide_expense, is_deleted) VALUES (?, 'PUBLIC', FALSE, TRUE)",
                UUID.randomUUID());
        jdbcTemplate.update(
                "INSERT INTO comment (id, user_id, post_id, is_deleted) VALUES (?, ?, ?, FALSE)",
                UUID.randomUUID(),
                activeUserId,
                postId);
        jdbcTemplate.update(
                "INSERT INTO comment (id, user_id, post_id, is_deleted) VALUES (?, ?, ?, NULL)",
                UUID.randomUUID(),
                activeUserId,
                postId);
        jdbcTemplate.update(
                "INSERT INTO comment (id, user_id, post_id, is_deleted) VALUES (?, ?, ?, TRUE)",
                UUID.randomUUID(),
                activeUserId,
                postId);
        jdbcTemplate.update(
                "INSERT INTO itinerary (id, is_deleted) VALUES (?, FALSE), (?, FALSE), (?, TRUE)",
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID());
        jdbcTemplate.update(
                "INSERT INTO groups (id, is_deleted) VALUES (?, FALSE), (?, TRUE)",
                UUID.randomUUID(),
                UUID.randomUUID());

        insertReport("PENDING", activeUserId, lockedUserId);
        insertReport("PROCESSED", activeUserId, lockedUserId);
        insertReport("PROCESSED", activeUserId, lockedUserId);
        insertReport("DISMISSED", activeUserId, lockedUserId);

        jdbcTemplate.update(
                "INSERT INTO moderation_action (id, user_id, ba_id) VALUES (?, ?, ?), (?, ?, ?)",
                UUID.randomUUID(),
                lockedUserId,
                activeUserId,
                UUID.randomUUID(),
                lockedUserId,
                activeUserId);
    }

    @Test
    void getOverview_countsOnlyCurrentBusinessRecords() {
        AdminDashboardOverviewProjection overview = adminDashboardRepository.getOverview();

        assertAll(
                () -> assertEquals(2, overview.getTotalUsers()),
                () -> assertEquals(1, overview.getLockedUsers()),
                () -> assertEquals(1, overview.getTotalPosts()),
                () -> assertEquals(2, overview.getTotalComments()),
                () -> assertEquals(2, overview.getTotalItineraries()),
                () -> assertEquals(1, overview.getTotalGroups()),
                () -> assertEquals(1, overview.getPendingReports()),
                () -> assertEquals(2, overview.getProcessedReports()),
                () -> assertEquals(1, overview.getDismissedReports()),
                () -> assertEquals(2, overview.getTotalModerationActions()));
    }

    private void insertReport(String status, UUID reporterId, UUID reportedUserId) {
        jdbcTemplate.update(
                """
				INSERT INTO report_content (
					id, content_type, target_id, report_type, status, reporter_id, reported_user_id
				) VALUES (?, 'POST', ?, 'SPAM', ?, ?, ?)
				""",
                UUID.randomUUID(),
                UUID.randomUUID(),
                status,
                reporterId,
                reportedUserId);
    }
}
