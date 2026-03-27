package com.tripjoy.api.entity;

import jakarta.persistence.*;

import lombok.*;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TravelNotebook extends BaseEntity {

    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "weather_forecast", columnDefinition = "TEXT")
    private String weatherForecast;

    @Column(name = "culture_etiquette", columnDefinition = "TEXT")
    private String cultureEtiquette;

    @Column(name = "emergency_contacts", columnDefinition = "TEXT")
    private String emergencyContacts;

    @Column(name = "packing_guide", columnDefinition = "TEXT")
    private String packingGuide;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "itinerary_id")
    private Itinerary itinerary;
}
