package com.recruting.demo.security.service;

import com.recruting.demo.security.dto.RegisterRequest;
import com.recruting.demo.security.dto.RegisterResponse;
import com.recruting.demo.security.entity.AppRole;
import com.recruting.demo.security.entity.AppUser;
import com.recruting.demo.security.entity.RoleName;
import com.recruting.demo.security.exception.BadRequestException;
import com.recruting.demo.security.repository.AppRoleRepository;
import com.recruting.demo.security.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RegistrationService {

    private final AppUserRepository appUserRepository;
    private final AppRoleRepository appRoleRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        String username = request.getUsername().trim();
        String email = request.getEmail().trim().toLowerCase();

        if (appUserRepository.existsByUsername(username)) {
            throw new BadRequestException("Логин уже занят");
        }
        if (appUserRepository.existsByEmail(email)) {
            throw new BadRequestException("Email уже используется");
        }

        validatePassword(request.getPassword());

        Set<AppRole> rolesToAssign = resolveRoles(request.getRoles());

        AppUser user = new AppUser();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setEnabled(true);
        user.setRoles(rolesToAssign);

        AppUser saved = appUserRepository.save(user);

        return RegisterResponse.builder()
                .id(saved.getId())
                .username(saved.getUsername())
                .email(saved.getEmail())
                .roles(saved.getRoles().stream()
                        .map(role -> role.getName().name())
                        .collect(Collectors.toSet()))
                .build();
    }

    private Set<AppRole> resolveRoles(Set<RoleName> requestedRoles) {
        Set<RoleName> roles = new HashSet<>();

        if (requestedRoles == null || requestedRoles.isEmpty()) {
            roles.add(RoleName.ROLE_CANDIDATE);
        } else {
            roles.addAll(requestedRoles.stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet()));
        }

        if (roles.contains(RoleName.ROLE_ADMIN)) {
            throw new BadRequestException("Назначение ROLE_ADMIN при регистрации запрещено");
        }

        Set<AppRole> resolvedRoles = roles.stream()
                .map(roleName -> appRoleRepository.findByName(roleName)
                        .orElseThrow(() -> new BadRequestException("Роль " + roleName + " не настроена")))
                .collect(Collectors.toSet());

        if (resolvedRoles.isEmpty()) {
            throw new BadRequestException("Не переданы роли для назначения");
        }

        return resolvedRoles;
    }

    private void validatePassword(String password) {
        if (password == null || password.length() < 8) {
            throw new BadRequestException("Пароль должен содержать минимум 8 символов");
        }

        boolean hasSpecial = password.chars().anyMatch(ch -> !Character.isLetterOrDigit(ch));
        if (!hasSpecial) {
            throw new BadRequestException("Пароль должен содержать хотя бы один спецсимвол");
        }
    }
}
