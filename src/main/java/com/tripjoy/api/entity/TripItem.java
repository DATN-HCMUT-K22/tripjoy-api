package com.tripjoy.api.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;

import lombok.*;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "trip_item")
public class TripItem extends BaseEntity {

    private LocalDateTime startTime;

    private Integer duration;

    @Column(columnDefinition = "TEXT")
    private String note;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private Location location;

    /** Tên địa điểm thô từ AI (Dự phòng nếu location object chưa được enrich) */
    @Column(name = "raw_location_name", columnDefinition = "TEXT")
    private String rawLocationName;

    /** Place ID thô từ AI (Dự phòng) */
    @Column(name = "raw_place_id")
    private String rawPlaceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "itinerary_id")
    private Itinerary itinerary;
}
