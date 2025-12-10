package com.tripjoy.api.repository;

import com.tripjoy.api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    // SELECT * FROM users WHERE is_deleted = false
    @Override
    List<User> findAll();

    @Query(value = "SELECT * FROM users", nativeQuery = true)
    List<User> findAllIncludingDeleted();

    @Query(value = "SELECT * FROM users WHERE is_deleted = true", nativeQuery = true)
    List<User> findAllOnlyDeleted();

    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    Optional<User> findByUsername(String username);
}
