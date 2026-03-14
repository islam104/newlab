package com.recruting.demo.license.repository;

import com.recruting.demo.license.entity.DeviceLicense;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DeviceLicenseRepository extends JpaRepository<DeviceLicense, UUID> {
    long countByLicenseId(UUID licenseId);

    boolean existsByLicenseIdAndDeviceId(UUID licenseId, UUID deviceId);
}
