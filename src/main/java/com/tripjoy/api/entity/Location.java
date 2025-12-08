package com.tripjoy.api.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Location extends BaseEntity {

    private String name;
    private Double lat;
    private Double lng;
    private String hotline;
    private String category;
    private Boolean isOpen;

    @OneToMany(mappedBy = "location", fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<LocationInfo> locationInfos = new HashSet<>();
}
