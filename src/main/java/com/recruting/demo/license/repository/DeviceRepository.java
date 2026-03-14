package com.recruting.demo.license.repository;

import com.recruting.demo.license.entity.Device;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DeviceRepository extends JpaRepository<Device, UUID> {
    Optional<Device> findByMacAddress(String macAddress);
}
