package com.tripjoy.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;

import lombok.*;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Hashtag extends BaseEntity {

    @Column(unique = true, nullable = false)
    private String name;
}
