package com.tripjoy.api.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Location extends BaseEntity {

    private String name;
    private Long lat;
    private Long lng;
    private String hotline;
    private String category;
    private Boolean isOpen;

    @OneToMany(mappedBy = "location", fetch = FetchType.LAZY)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Set<LocationInfo> locationInfos = new HashSet<>();
}
