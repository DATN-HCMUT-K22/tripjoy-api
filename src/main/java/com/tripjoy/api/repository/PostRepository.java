package com.tripjoy.api.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tripjoy.api.entity.Post;

@Repository
public interface PostRepository extends JpaRepository<Post, UUID> {

    /**
     * Enterprise Search for Posts
     * Uses PostgreSQL Full-Text Search (GIN), ILIKE fallback, and B-Tree indexes for Itinerary fields.
     * All parameters are optional - dynamically skipped if NULL.
     */
    @Query(
            value =
                    """
		SELECT p.* FROM post p
		LEFT JOIN post_hashtag_mapping phm ON phm.post_id = p.id
		LEFT JOIN hashtag h ON phm.hashtag_id = h.id
		LEFT JOIN itinerary i ON p.itinerary_id = i.id
		WHERE p.is_deleted = false
		AND (:keyword IS NULL OR (
			to_tsvector('simple', f_unaccent(coalesce(p.content, ''))) @@ plainto_tsquery('simple', f_unaccent(CAST(:keyword AS text)))
			OR f_unaccent(p.content) ILIKE '%' || f_unaccent(CAST(:keyword AS text)) || '%'
		))
		AND (:hashtag IS NULL OR LOWER(h.name) = LOWER(CAST(:hashtag AS text)))
		AND (CAST(:creatorId AS uuid) IS NULL OR p.creator_id = CAST(:creatorId AS uuid))
		AND (CAST(:itineraryId AS uuid) IS NULL OR p.itinerary_id = CAST(:itineraryId AS uuid))
		AND (CAST(:startDate AS timestamp) IS NULL OR i.start_date >= CAST(:startDate AS timestamp))
		AND (CAST(:endDate AS timestamp) IS NULL OR i.end_date <= CAST(:endDate AS timestamp))
		AND (CAST(:minDays AS integer) IS NULL OR EXTRACT(DAY FROM (i.end_date - i.start_date)) >= CAST(:minDays AS integer))
		AND (CAST(:maxDays AS integer) IS NULL OR EXTRACT(DAY FROM (i.end_date - i.start_date)) <= CAST(:maxDays AS integer))
		AND (CAST(:minBudget AS numeric) IS NULL OR i.budget_estimate >= CAST(:minBudget AS numeric))
		AND (CAST(:maxBudget AS numeric) IS NULL OR i.budget_estimate <= CAST(:maxBudget AS numeric))
		AND (CAST(:minPeople AS integer) IS NULL OR i.people_quantity >= CAST(:minPeople AS integer))
		AND (CAST(:maxPeople AS integer) IS NULL OR i.people_quantity <= CAST(:maxPeople AS integer))
		AND (CAST(:originId AS uuid) IS NULL OR i.origin = CAST(:originId AS uuid))
		AND (CAST(:destinationId AS uuid) IS NULL OR i.destination = CAST(:destinationId AS uuid))
		GROUP BY p.id
		ORDER BY
			CASE WHEN :sortBy = 'relevance' THEN ts_rank(to_tsvector('simple', f_unaccent(coalesce(p.content, ''))), plainto_tsquery('simple', f_unaccent(coalesce(CAST(:keyword AS text), '')))) END DESC,
			p.created_at DESC
		LIMIT :limit OFFSET :offset
		""",
            nativeQuery = true)
    List<Post> searchPosts(
            @Param("keyword") String keyword,
            @Param("hashtag") String hashtag,
            @Param("creatorId") UUID creatorId,
            @Param("itineraryId") UUID itineraryId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("minDays") Integer minDays,
            @Param("maxDays") Integer maxDays,
            @Param("minBudget") BigDecimal minBudget,
            @Param("maxBudget") BigDecimal maxBudget,
            @Param("minPeople") Integer minPeople,
            @Param("maxPeople") Integer maxPeople,
            @Param("originId") UUID originId,
            @Param("destinationId") UUID destinationId,
            @Param("sortBy") String sortBy,
            @Param("limit") int limit,
            @Param("offset") int offset);

    /**
     * Count total search results (for pagination metadata).
     */
    @Query(
            value =
                    """
		SELECT COUNT(DISTINCT p.id) FROM post p
		LEFT JOIN post_hashtag_mapping phm ON phm.post_id = p.id
		LEFT JOIN hashtag h ON phm.hashtag_id = h.id
		LEFT JOIN itinerary i ON p.itinerary_id = i.id
		WHERE p.is_deleted = false
		AND (:keyword IS NULL OR (
			to_tsvector('simple', f_unaccent(coalesce(p.content, ''))) @@ plainto_tsquery('simple', f_unaccent(CAST(:keyword AS text)))
			OR f_unaccent(p.content) ILIKE '%' || f_unaccent(CAST(:keyword AS text)) || '%'
		))
		AND (:hashtag IS NULL OR LOWER(h.name) = LOWER(CAST(:hashtag AS text)))
		AND (CAST(:creatorId AS uuid) IS NULL OR p.creator_id = CAST(:creatorId AS uuid))
		AND (CAST(:itineraryId AS uuid) IS NULL OR p.itinerary_id = CAST(:itineraryId AS uuid))
		AND (CAST(:startDate AS timestamp) IS NULL OR i.start_date >= CAST(:startDate AS timestamp))
		AND (CAST(:endDate AS timestamp) IS NULL OR i.end_date <= CAST(:endDate AS timestamp))
		AND (CAST(:minDays AS integer) IS NULL OR EXTRACT(DAY FROM (i.end_date - i.start_date)) >= CAST(:minDays AS integer))
		AND (CAST(:maxDays AS integer) IS NULL OR EXTRACT(DAY FROM (i.end_date - i.start_date)) <= CAST(:maxDays AS integer))
		AND (CAST(:minBudget AS numeric) IS NULL OR i.budget_estimate >= CAST(:minBudget AS numeric))
		AND (CAST(:maxBudget AS numeric) IS NULL OR i.budget_estimate <= CAST(:maxBudget AS numeric))
		AND (CAST(:minPeople AS integer) IS NULL OR i.people_quantity >= CAST(:minPeople AS integer))
		AND (CAST(:maxPeople AS integer) IS NULL OR i.people_quantity <= CAST(:maxPeople AS integer))
		AND (CAST(:originId AS uuid) IS NULL OR i.origin = CAST(:originId AS uuid))
		AND (CAST(:destinationId AS uuid) IS NULL OR i.destination = CAST(:destinationId AS uuid))
		""",
            nativeQuery = true)
    long countSearchPosts(
            @Param("keyword") String keyword,
            @Param("hashtag") String hashtag,
            @Param("creatorId") UUID creatorId,
            @Param("itineraryId") UUID itineraryId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("minDays") Integer minDays,
            @Param("maxDays") Integer maxDays,
            @Param("minBudget") BigDecimal minBudget,
            @Param("maxBudget") BigDecimal maxBudget,
            @Param("minPeople") Integer minPeople,
            @Param("maxPeople") Integer maxPeople,
            @Param("originId") UUID originId,
            @Param("destinationId") UUID destinationId);

    @EntityGraph(attributePaths = {"creator", "itinerary"})
    Page<Post> findBySoftDeleteInfoIsDeletedFalse(Pageable pageable);

    @EntityGraph(attributePaths = {"creator", "itinerary"})
    Page<Post> findBySaveUsersIdAndSoftDeleteInfoIsDeletedFalse(UUID userId, Pageable pageable);

    @Query(
            "SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM Post p JOIN p.likeUsers u WHERE p.id = :postId AND u.id = :userId")
    boolean isLikedByUser(@Param("postId") UUID postId, @Param("userId") UUID userId);

    @Query(
            "SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM Post p JOIN p.saveUsers u WHERE p.id = :postId AND u.id = :userId")
    boolean isSavedByUser(@Param("postId") UUID postId, @Param("userId") UUID userId);

    @Query("SELECT p.id FROM Post p JOIN p.likeUsers u WHERE p.id IN :postIds AND u.id = :userId")
    List<UUID> findLikedPostIdsByUser(@Param("postIds") List<UUID> postIds, @Param("userId") UUID userId);

    @Query("SELECT p.id FROM Post p JOIN p.saveUsers u WHERE p.id IN :postIds AND u.id = :userId")
    List<UUID> findSavedPostIdsByUser(@Param("postIds") List<UUID> postIds, @Param("userId") UUID userId);

    /**
     * Count posts created after a specific date
     */
    @Query("SELECT COUNT(p) FROM Post p WHERE p.createdAt >= :date")
    long countCreatedAfter(@Param("date") LocalDateTime date);

    /**
     * Count posts by deleted status
     */
    @Query("SELECT COUNT(p) FROM Post p WHERE p.softDeleteInfo.isDeleted = :isDeleted")
    long countByIsDeleted(@Param("isDeleted") boolean isDeleted);
}
