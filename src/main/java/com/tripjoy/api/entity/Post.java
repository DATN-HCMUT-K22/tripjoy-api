package com.tripjoy.api.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tripjoy.api.entity.embeddable.SoftDeleteInfo;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Post extends BaseEntity {

        private String mediaUrl;

        @Embedded
        private SoftDeleteInfo softDeleteInfo = new SoftDeleteInfo();

        private String content;
        private Integer shareQuantity;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "itinerary_id")
        private Itinerary itinerary;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "creator_id")
        private User creator;

        @OneToMany(mappedBy = "post", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
        @JsonIgnore
        private Set<Comment> comments = new HashSet<>();

        @OneToMany(mappedBy = "post", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
        @JsonIgnore
        private Set<PostHashtag> hashtags = new HashSet<>();

        @ManyToMany
        @JoinTable(name = "save_post", joinColumns = @JoinColumn(name = "post_id"), inverseJoinColumns = @JoinColumn(name = "user_id"))
        private Set<User> saveUsers = new HashSet<>();

        @ManyToMany
        @JoinTable(name = "like_post", joinColumns = @JoinColumn(name = "post_id"), inverseJoinColumns = @JoinColumn(name = "user_id"))
        private Set<User> likeUsers = new HashSet<>();
}
