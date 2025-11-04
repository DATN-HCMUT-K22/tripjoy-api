package com.tripjoy.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Lob;

public class Post extends BaseEntity {
    @Lob
    @Column(columnDefinition = "TEXT")
    String content;
    long shareCount = 0;
    String mediaUrl;
    @Column(nullable = false)
    boolean isPublic = true;
    boolean isDeleted = false;
}
