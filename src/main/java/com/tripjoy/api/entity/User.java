package com.tripjoy.api.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.LocalDate;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "\"user\"")
@SQLDelete(sql = "UPDATE \"user\" SET is_deleted = true WHERE id = ?")
@Where(clause = "is_deleted = false")
public class User extends BaseEntity {

    @Column(unique = true, nullable = false)
    String username;
    String password;

    @Column(unique = true)
    String email;
    boolean isEmailVerified = false;

    String phoneNumber;
    String fullName;

    String bio;
    String avatarUrl;
    LocalDate dateOfBirth;

    boolean isDeleted = true;
    boolean isLocked = false;

    @ManyToMany
    Set<Role> roles;
}
