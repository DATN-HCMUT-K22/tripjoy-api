package com.tripjoy.api.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;


@Data
@Entity
public class Itinerary extends BaseEntity{

    private String name;
    private String description;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer peopleQuantity;
    private Long budgetEstimate;
    private String status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Groups group;

    @OneToMany(mappedBy = "itinerary", fetch = FetchType.LAZY)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Set<ItineraryTheme> itineraryThemes = new HashSet<>();

    @OneToMany(mappedBy = "itinerary", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Set<TripItem> tripItems = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "favourite_itinerary",
            joinColumns = @JoinColumn(name = "itinerary_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> favouriteUsers = new HashSet<>();
}
