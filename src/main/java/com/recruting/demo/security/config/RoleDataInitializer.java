package com.recruting.demo.security.config;

import com.recruting.demo.security.entity.AppRole;
import com.recruting.demo.security.entity.RoleName;
import com.recruting.demo.security.repository.AppRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class RoleDataInitializer {

    private final AppRoleRepository appRoleRepository;

    @Bean
    CommandLineRunner initRoles() {
        return args -> {
            for (RoleName roleName : RoleName.values()) {
                if (!appRoleRepository.existsByName(roleName)) {
                    AppRole role = new AppRole();
                    role.setName(roleName);
                    appRoleRepository.save(role);
                }
            }
        };
    }
}
