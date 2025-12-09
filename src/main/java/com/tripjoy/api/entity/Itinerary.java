package com.tripjoy.api.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;


@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Itinerary extends BaseEntity{

    private String name;
    private String description;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer peopleQuantity;
    private Long budgetEstimate;
    private String status;
    private String destination;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private Group group;

    @OneToMany(mappedBy = "itinerary", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonIgnore
    private Set<ItineraryTheme> itineraryThemes;

    @OneToMany(mappedBy = "itinerary", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonIgnore
    private Set<TripItem> tripItems;

    @OneToMany(mappedBy = "itinerary", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonIgnore
    private Set<Expense> expenses = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "favourite_itinerary",
            joinColumns = @JoinColumn(name = "itinerary_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> favouriteUsers = new HashSet<>();
}
