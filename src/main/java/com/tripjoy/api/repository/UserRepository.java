package com.tripjoy.api.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tripjoy.api.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    // SELECT * FROM users WHERE is_deleted = false
    @Override
    List<User> findAll();

    /** Paginated version of findAll — for admin list endpoint. */
    Page<User> findAll(Pageable pageable);

    @Query(value = "SELECT * FROM users", nativeQuery = true)
    List<User> findAllIncludingDeleted();

    @Query(value = "SELECT * FROM users WHERE is_deleted = true", nativeQuery = true)
    List<User> findAllOnlyDeleted();

    @Query(value = """
            SELECT * FROM users u
            WHERE (lower(unaccent(u.username)) LIKE lower(unaccent(CONCAT('%', :keyword, '%')))
                OR lower(unaccent(u.full_name)) LIKE lower(unaccent(CONCAT('%', :keyword, '%')))
                OR lower(unaccent(u.email))     LIKE lower(unaccent(CONCAT('%', :keyword, '%')))
                OR u.phone_number LIKE CONCAT('%', :keyword, '%'))
              AND u.is_deleted = false
            """, nativeQuery = true)
    Page<User> searchGlobalUsers(@Param("keyword") String keyword, Pageable pageable);

    /**
     * Paginated keyword search across username and email.
     * Used by {@code GET /users?q=keyword} (admin panel).
     */
    @Query(value = """
            SELECT * FROM users u
            WHERE (lower(unaccent(u.username)) LIKE lower(unaccent(CONCAT('%', :keyword, '%')))
                OR lower(unaccent(u.email))    LIKE lower(unaccent(CONCAT('%', :keyword, '%'))))
              AND u.is_deleted = false
            """, nativeQuery = true)
    Page<User> searchByUsernameOrEmailPaged(@Param("keyword") String keyword, Pageable pageable);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    Optional<User> findByUsername(String username);

    @Query(value = """
            SELECT * FROM users u
            WHERE (lower(unaccent(u.username)) LIKE lower(unaccent(CONCAT('%', :keyword, '%')))
                OR lower(unaccent(u.email))    LIKE lower(unaccent(CONCAT('%', :keyword, '%'))))
              AND u.is_deleted = false
            """, nativeQuery = true)
    List<User> searchByUsernameOrEmail(@Param("keyword") String keyword);
}
