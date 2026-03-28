package com.tripjoy.api.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tripjoy.api.entity.embeddable.SoftDeleteInfo;
import com.tripjoy.api.enums.ItineraryStatus;
import org.hibernate.annotations.BatchSize;

import lombok.*;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "itinerary",
        indexes = {@Index(name = "idx_itinerary_group", columnList = "group_id, is_deleted")})
public class Itinerary extends BaseEntity {

    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private Integer peopleQuantity;

    @Column(name = "budget_estimate", precision = 19, scale = 2)
    private BigDecimal budgetEstimate;

    @Enumerated(EnumType.STRING)
    private ItineraryStatus status;

    @Embedded
    @Builder.Default
    private SoftDeleteInfo softDeleteInfo = new SoftDeleteInfo();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private Group group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "origin")
    private Location origin;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination")
    private Location destination;

    @ManyToMany
    @JoinTable(
        name = "itinerary_theme_mapping",
        joinColumns = @JoinColumn(name = "itinerary_id"),
        inverseJoinColumns = @JoinColumn(name = "theme_id")
    )
    @BatchSize(size = 20)
    @Builder.Default
    private Set<Theme> themes = new HashSet<>();

    @OneToMany(mappedBy = "itinerary", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    @Builder.Default
    private Set<TripItem> tripItems = new HashSet<>();

    @OneToMany(mappedBy = "itinerary", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    @Builder.Default
    private Set<Expense> expenses = new HashSet<>();

    @OneToOne(mappedBy = "itinerary", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private TravelNotebook travelNotebook;

    @ManyToMany
    @JoinTable(
            name = "favourite_itinerary",
            joinColumns = @JoinColumn(name = "itinerary_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"))
    @Builder.Default
    private Set<User> favouriteUsers = new HashSet<>();
}
