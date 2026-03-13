package com.recruting.demo.security.controller;

import com.recruting.demo.security.dto.RegisterRequest;
import com.recruting.demo.security.dto.RegisterResponse;
import com.recruting.demo.security.dto.LoginRequest;
import com.recruting.demo.security.dto.RefreshTokenRequest;
import com.recruting.demo.security.dto.TokenPairResponse;
import com.recruting.demo.security.service.RegistrationService;
import com.recruting.demo.security.service.TokenPairService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping({"/api/auth", "/auth"})
@RequiredArgsConstructor
public class AuthController {

    private final RegistrationService registrationService;
    private final TokenPairService tokenPairService;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        RegisterResponse response = registrationService.register(request);
        return ResponseEntity.status(201).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<TokenPairResponse> login(@Valid @RequestBody LoginRequest request) {
        TokenPairResponse response = tokenPairService.login(request.getUsername(), request.getPassword());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenPairResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        TokenPairResponse response = tokenPairService.refresh(request.getRefreshToken());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/csrf")
    public Map<String, String> csrf(@Nullable CsrfToken csrfToken) {
        if (csrfToken == null) {
            return Map.of(
                    "enabled", "false",
                    "message", "CSRF отключен. Для авторизации используйте Bearer access token."
            );
        }
        return Map.of(
                "token", csrfToken.getToken(),
                "headerName", csrfToken.getHeaderName(),
                "parameterName", csrfToken.getParameterName()
        );
    }
}
