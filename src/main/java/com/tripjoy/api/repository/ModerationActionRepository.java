package com.tripjoy.api.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tripjoy.api.entity.ModerationAction;

@Repository
public interface ModerationActionRepository extends JpaRepository<ModerationAction, UUID> {}
