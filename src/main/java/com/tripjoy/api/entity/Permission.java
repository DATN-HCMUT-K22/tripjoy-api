package com.tripjoy.api.entity;

import jakarta.persistence.Entity;

import lombok.*;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Permission extends BaseEntity {

    private String name;
    private String description;
}
