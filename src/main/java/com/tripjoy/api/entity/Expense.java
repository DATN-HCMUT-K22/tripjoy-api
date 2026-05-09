package com.tripjoy.api.entity;

import java.math.BigDecimal;

import jakarta.persistence.*;

import lombok.*;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "expense", indexes = {@Index(name = "idx_expense_itinerary_id", columnList = "itinerary_id")})
public class Expense extends BaseEntity {

    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String type;
    private String method;

    @Column(precision = 19, scale = 2)
    private BigDecimal amount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "itinerary_id", nullable = false)
    private Itinerary itinerary;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
