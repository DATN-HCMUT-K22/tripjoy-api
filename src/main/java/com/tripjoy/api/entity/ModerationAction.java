package com.tripjoy.api.entity;

import jakarta.persistence.*;

import lombok.*;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModerationAction extends BaseEntity {

    @Column(length = 80)
    private String actionType;

    @Column(columnDefinition = "TEXT")
    private String note;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ba_id", nullable = false)
    private User ba;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_content_id")
    private ReportContent reportContent;
}
