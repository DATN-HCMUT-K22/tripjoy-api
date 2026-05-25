package com.tripjoy.api.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tripjoy.api.entity.Expense;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, UUID> {

    /** Fetch all expenses for an itinerary (no filter). */
    List<Expense> findByItineraryId(UUID itineraryId);

    /** Fetch expenses filtered by the user who paid. */
    List<Expense> findByItineraryIdAndPaidById(UUID itineraryId, UUID paidById);

    /**
     * Aggregate total paid amount and expense count per payer within an itinerary.
     * Returns rows of [paidBy user entity ref, total amount, count].
     * Uses JPQL — paidBy is guaranteed non-null because we default it to the creator.
     */
    @Query("""
            SELECT e.paidBy.id, SUM(e.amount), COUNT(e)
            FROM Expense e
            WHERE e.itinerary.id = :itineraryId
            GROUP BY e.paidBy.id
            """)
    List<Object[]> findPayerSummaryByItineraryId(@Param("itineraryId") UUID itineraryId);

    /**
     * Grand total of all expenses within an itinerary.
     * Returns Optional.empty() when there are no expenses (COALESCE handled in service).
     */
    @Query("SELECT SUM(e.amount) FROM Expense e WHERE e.itinerary.id = :itineraryId")
    Optional<BigDecimal> sumAmountByItineraryId(@Param("itineraryId") UUID itineraryId);
}
