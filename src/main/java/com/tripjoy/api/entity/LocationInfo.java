package com.tripjoy.api.entity;

import jakarta.persistence.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class LocationInfo extends BaseEntity{

    private String content;
    private String contentType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Location location;

}
