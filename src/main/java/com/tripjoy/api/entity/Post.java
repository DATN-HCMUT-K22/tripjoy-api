package com.tripjoy.api.entity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tripjoy.api.entity.embeddable.SoftDeleteInfo;
import org.hibernate.annotations.BatchSize;
import com.tripjoy.api.enums.PostVisibility;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "post",
        indexes = {
                @Index(name = "idx_post_creator_deleted", columnList = "creator_id, is_deleted"),
                @Index(name = "idx_post_created_at", columnList = "created_at DESC")
        })
public class Post extends BaseEntity {

    @Builder.Default
    @ElementCollection
    @CollectionTable(name = "post_media", joinColumns = @JoinColumn(name = "post_id"))
    @Column(name = "media_url", length = 1024)
    @OrderColumn(name = "media_order")
    @BatchSize(size = 20)
    private List<String> mediaUrls = new ArrayList<>();

    @Embedded
    @Builder.Default
    private SoftDeleteInfo softDeleteInfo = new SoftDeleteInfo();

    private String content;
    private Integer shareQuantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private PostVisibility visibility = PostVisibility.PUBLIC;

    @Builder.Default
    @Column(name = "like_count")
    private Long likeCount = 0L;

    @Builder.Default
    @Column(name = "comment_count")
    private Long commentCount = 0L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "itinerary_id")
    private Itinerary itinerary;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id")
    private User creator;

    @OneToMany(mappedBy = "post", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonIgnore
    @Builder.Default
    private Set<Comment> comments = new HashSet<>();

    @ManyToMany
    @JoinTable(
        name = "post_hashtag_mapping",
        joinColumns = @JoinColumn(name = "post_id"),
        inverseJoinColumns = @JoinColumn(name = "hashtag_id")
    )
    @BatchSize(size = 20)
    @Builder.Default
    private Set<Hashtag> hashtags = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "save_post",
            joinColumns = @JoinColumn(name = "post_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"))
    @Builder.Default
    private Set<User> saveUsers = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "like_post",
            joinColumns = @JoinColumn(name = "post_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"))
    @Builder.Default
    private Set<User> likeUsers = new HashSet<>();
}
