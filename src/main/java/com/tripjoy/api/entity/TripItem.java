package com.tripjoy.api.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import com.tripjoy.api.enums.TripItemStatus;
import lombok.*;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "trip_item",
        indexes = {@Index(name = "idx_trip_item_itinerary_id", columnList = "itinerary_id")})
public class TripItem extends BaseEntity {

    private LocalDateTime startTime;

    private Integer duration;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private TripItemStatus status = TripItemStatus.PENDING;

    @Column(columnDefinition = "TEXT")
    private String note;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private Location location;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "itinerary_id")
    private Itinerary itinerary;
}
