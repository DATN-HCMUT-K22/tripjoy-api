package com.tripjoy.api.entity;

import jakarta.persistence.*;

import lombok.*;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HandleReportContent extends BaseEntity {

    @Column(name = "report_type", length = 80)
    private String reportType;

    @Column(length = 30)
    private String status;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_content_id", nullable = false)
    private ReportContent reportContent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ba_id", nullable = false)
    private User ba;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "moderation_action_id")
    private ModerationAction moderationAction;
}
