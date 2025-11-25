package com.tripjoy.api.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Users extends BaseEntity {

    private String username;
    private String password;
    private String email;
    private Boolean isEmailVerified = false;
    private String phoneNumber;
    private String fullName;
    private String bio;
    private String avatarUrl;
    private LocalDateTime dateOfBirth;
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
    private Set<Itinerary> favouriteItineraries = new HashSet<>();

}
