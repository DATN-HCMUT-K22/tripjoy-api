package com.tripjoy.api.entity;

import jakarta.persistence.Entity;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ReportContent extends BaseEntity {

    private String contentType;
    private String text;
    private String mediaUrl;
    private String status;
}
