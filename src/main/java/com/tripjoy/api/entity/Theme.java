package com.tripjoy.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Theme extends BaseEntity {

    @Column(unique = true, nullable = false)
    private String name;
}
