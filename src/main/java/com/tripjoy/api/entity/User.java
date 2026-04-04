package com.tripjoy.api.entity;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tripjoy.api.entity.embeddable.SoftDeleteInfo;

import lombok.*;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class User extends BaseEntity {

    private String username;
    private String password;
    private String email;

    @Builder.Default
    private Boolean isEmailVerified = false;

    private String phoneNumber;
    private String fullName;
    private String bio;
    private String avatarUrl;
    private LocalDate dateOfBirth;
    private Long credits;

    @Embedded
    @Builder.Default
    private SoftDeleteInfo softDeleteInfo = new SoftDeleteInfo();

    @Builder.Default
    private Boolean isLocked = false;

    @ManyToMany
    @JoinTable(
            name = "user_role",
            joinColumns = @JoinColumn(name = "users_id"),
            inverseJoinColumns = @JoinColumn(name = "role_name"))
    private Set<Role> roles;

    @ManyToMany(mappedBy = "favouriteUsers")
    @JsonIgnore
    private Set<Itinerary> favouriteItineraries = new HashSet<>();
}
