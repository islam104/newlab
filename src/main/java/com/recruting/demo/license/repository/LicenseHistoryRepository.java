package com.recruting.demo.license.repository;

import com.recruting.demo.license.entity.LicenseHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LicenseHistoryRepository extends JpaRepository<LicenseHistory, UUID> {
}
