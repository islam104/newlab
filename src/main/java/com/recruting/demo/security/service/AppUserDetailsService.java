package com.recruting.demo.security.service;

import com.recruting.demo.security.entity.AppUser;
import com.recruting.demo.security.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AppUserDetailsService implements UserDetailsService {

    private final AppUserRepository appUserRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AppUser appUser = appUserRepository.findByUsernameOrEmail(username, username)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден: " + username));

        return User.withUsername(appUser.getUsername())
                .password(appUser.getPasswordHash())
                .accountExpired(appUser.isAccountExpired())
                .accountLocked(appUser.isAccountLocked())
                .credentialsExpired(appUser.isCredentialsExpired())
                .disabled(appUser.isDisabled())
                .authorities(appUser.getRoles().stream()
                        .map(role -> new SimpleGrantedAuthority(role.getName().name()))
                        .collect(Collectors.toSet()))
                .build();
    }
}
