package com.tripjoy.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tripjoy.api.entity.Permission;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, String> {}
