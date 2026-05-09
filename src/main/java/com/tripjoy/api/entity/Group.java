package com.tripjoy.api.entity;

import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.Formula;
import org.hibernate.annotations.BatchSize;

import jakarta.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tripjoy.api.entity.embeddable.SoftDeleteInfo;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "groups")
public class Group extends BaseEntity {

    private String name;
    private String description;
    private Integer chatbotCount;
    private String avatar;
    private String themeColor;
    private Boolean isPro;

    @Formula("(SELECT count(i.id) FROM itinerary i WHERE i.group_id = id AND i.is_deleted = false)")
    private Integer itiCount;

    @Embedded
    @Builder.Default
    private SoftDeleteInfo softDeleteInfo = new SoftDeleteInfo();

    @OneToMany(mappedBy = "group", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @BatchSize(size = 20)
    @JsonIgnore
    @Builder.Default
    private Set<Itinerary> itineraries = new HashSet<>();

    @OneToMany(mappedBy = "group", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @BatchSize(size = 20)
    @JsonIgnore
    @Builder.Default
    private Set<GroupMember> members = new HashSet<>();

    @OneToMany(mappedBy = "group", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonIgnore
    @Builder.Default
    private Set<Conversation> conversations = new HashSet<>();
}
