package com.tripjoy.api.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;

import com.tripjoy.api.converter.StringListConverter;

import lombok.*;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "expense",
        indexes = {
            @Index(name = "idx_expense_itinerary_id", columnList = "itinerary_id"),
            @Index(name = "idx_expense_itinerary_paid_by", columnList = "itinerary_id, paid_by"),
            @Index(name = "idx_expense_trip_item_id", columnList = "trip_item_id")
        })
public class Expense extends BaseEntity {

    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String type;
    private String method;

    @Column(precision = 19, scale = 2)
    private BigDecimal amount;

    /**
     * Comma-separated list of image URLs serving as receipt/invoice evidence.
     * Stored as plain TEXT using {@link StringListConverter}.
     */
    @Convert(converter = StringListConverter.class)
    @Column(name = "receipt_image_urls", columnDefinition = "TEXT")
    @Builder.Default
    private List<String> receiptImageUrls = new ArrayList<>();

    /**
     * The user who actually paid for this expense.
     * May differ from {@code user} (the person who recorded/created the expense entry).
     * Nullable — defaults to the creator when not explicitly specified.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paid_by")
    private User paidBy;

    /**
     * The actual timestamp when the payment was made.
     * May differ from {@code createdAt} (when the expense record was entered into the system).
     */
    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    /**
     * Optional association to a specific trip item within the itinerary.
     * Nullable — general expenses (e.g., travel insurance, transport) may not be tied to a location.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_item_id")
    private TripItem tripItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "itinerary_id", nullable = false)
    private Itinerary itinerary;

    /** The user who recorded/created this expense entry (derived from auth token). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
