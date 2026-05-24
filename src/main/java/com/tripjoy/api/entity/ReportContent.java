package com.tripjoy.api.entity;

import java.util.UUID;

import jakarta.persistence.*;

import lombok.*;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportContent extends BaseEntity {

    @Column(nullable = false, length = 30)
    private String contentType;

    @Column(nullable = false)
    private UUID targetId;

    @Column(nullable = false, length = 80)
    private String reportType;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String text;

    @Column(length = 1024)
    private String mediaUrl;

    @Column(nullable = false, length = 30)
    private String status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_user_id", nullable = false)
    private User reportedUser;
}
