package com.tripjoy.api.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItineraryTheme extends BaseEntity{

    private String theme;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "itinerary_id")
    private Itinerary itinerary;
}
