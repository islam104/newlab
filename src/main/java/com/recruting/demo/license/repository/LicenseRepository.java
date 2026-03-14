package com.recruting.demo.license.repository;

import com.recruting.demo.license.entity.License;
import com.recruting.demo.license.entity.Product;
import com.recruting.demo.security.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface LicenseRepository extends JpaRepository<License, UUID> {
    Optional<License> findByCode(String code);

    boolean existsByCode(String code);

    @Query("select l from License l join DeviceLicense dl on dl.license = l " +
            "where dl.device.id = :deviceId and l.user = :user " +
            "and l.product = :product and l.blocked = false and l.endingDate >= :now")
    Optional<License> findActiveByDeviceUserAndProduct(@Param("deviceId") UUID deviceId,
                                                       @Param("user") AppUser user,
                                                       @Param("product") Product product,
                                                       @Param("now") LocalDateTime now);
}
