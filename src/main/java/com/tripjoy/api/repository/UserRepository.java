package com.tripjoy.api.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.tripjoy.api.entity.User;

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

    @Query("SELECT u FROM User u WHERE "
            + "(LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) "
            + "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))) "
            + "AND u.softDeleteInfo.isDeleted = false")
    List<User> searchByUsernameOrEmail(@Param("keyword") String keyword);
}
