package com.tripjoy.api.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class ItineraryTheme extends BaseEntity{

    private String theme;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "itinerary_id")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Itinerary itinerary;
}
