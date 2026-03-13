package com.recruting.demo.security.repository;

import com.recruting.demo.security.entity.AppRole;
import com.recruting.demo.security.entity.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AppRoleRepository extends JpaRepository<AppRole, Long> {
    Optional<AppRole> findByName(RoleName name);

    boolean existsByName(RoleName name);
}
