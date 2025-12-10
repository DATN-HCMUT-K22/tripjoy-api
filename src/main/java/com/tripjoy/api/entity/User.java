package com.tripjoy.api.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

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
    private Boolean isEmailVerified = false;
    private String phoneNumber;
    private String fullName;
    private String bio;
    private String avatarUrl;
    private LocalDateTime dateOfBirth;
    private Long credits;
    private Boolean isDeleted = false;
    private Boolean isLocked = false;

    @ManyToMany
    @JoinTable(
            name = "user_role",
            joinColumns = @JoinColumn(name = "users_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles;

    @ManyToMany(mappedBy = "favouriteUsers")
    @JsonIgnore
    private Set<Itinerary> favouriteItineraries = new HashSet<>();

}
