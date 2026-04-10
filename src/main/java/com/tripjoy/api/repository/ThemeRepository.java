package com.tripjoy.api.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tripjoy.api.entity.Theme;

@Repository
public interface ThemeRepository extends JpaRepository<Theme, UUID> {
    Optional<Theme> findByNameIgnoreCase(String name);

    List<Theme> findByNameIn(Collection<String> names);
}
