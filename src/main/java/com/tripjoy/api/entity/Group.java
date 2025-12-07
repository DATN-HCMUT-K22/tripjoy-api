package com.tripjoy.api.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "groups")
public class Group extends BaseEntity{

    private String name;
    private Integer chatbotCount;
    private String avatar;
    private String themeColor;
    private Boolean isPro;

    @OneToMany(mappedBy = "group", fetch = FetchType.LAZY)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Set<Itinerary> itineraries = new HashSet<>();

}

