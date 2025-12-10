package com.tripjoy.api.entity;

import jakarta.persistence.Entity;
import lombok.*;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportContent extends BaseEntity{

    private String contentType;
    private String text;
    private String mediaUrl;
    private String status;
}
