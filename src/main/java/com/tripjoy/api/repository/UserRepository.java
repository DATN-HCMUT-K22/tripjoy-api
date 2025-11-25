package com.tripjoy.api.repository;

import com.tripjoy.api.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<Users, String> {
    // SELECT * FROM users WHERE is_deleted = false
    @Override
    List<Users> findAll();

    @Query(value = "SELECT * FROM users", nativeQuery = true)
    List<Users> findAllIncludingDeleted();

    @Query(value = "SELECT * FROM users WHERE is_deleted = true", nativeQuery = true)
    List<Users> findAllOnlyDeleted();

    boolean existsByUsername(String username);
    Optional<Users> findByUsername(String username);
}
