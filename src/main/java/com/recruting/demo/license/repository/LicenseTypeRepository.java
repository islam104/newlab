package com.recruting.demo.license.repository;

import com.recruting.demo.license.entity.LicenseType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface LicenseTypeRepository extends JpaRepository<LicenseType, UUID> {
    Optional<LicenseType> findByName(String name);
}
