package com.tripjoy.api.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tripjoy.api.entity.Hashtag;

@Repository
public interface HashtagRepository extends JpaRepository<Hashtag, UUID> {
    Optional<Hashtag> findByNameIgnoreCase(String name);

    List<Hashtag> findByNameIn(Collection<String> names);
}
