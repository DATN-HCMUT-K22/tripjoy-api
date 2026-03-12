package com.tripjoy.api.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import lombok.*;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HandleReportContent extends BaseEntity {

    private String report_type;
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_content_id", nullable = false)
    private ReportContent reportContent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ba_id", nullable = false)
    private User ba;
}
