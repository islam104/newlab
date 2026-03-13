package com.recruting.demo.security.service;

import com.recruting.demo.security.dto.TokenPairResponse;
import com.recruting.demo.security.entity.AppUser;
import com.recruting.demo.security.entity.SessionStatus;
import com.recruting.demo.security.entity.UserSession;
import com.recruting.demo.security.repository.AppUserRepository;
import com.recruting.demo.security.repository.UserSessionRepository;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TokenPairService {

    private final AuthenticationManager authenticationManager;
    private final AppUserRepository appUserRepository;
    private final UserSessionRepository userSessionRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public TokenPairResponse login(String username, String password) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );

        AppUser user = appUserRepository.findByUsernameOrEmail(authentication.getName(), authentication.getName())
                .orElseThrow(() -> new BadCredentialsException("Пользователь не найден"));

        UserSession session = createActiveSession(user);
        return issueTokenPair(user, session);
    }

    @Transactional
    public TokenPairResponse refresh(String refreshToken) {
        Claims claims = jwtTokenProvider.parseAndValidate(refreshToken, TokenType.REFRESH);

        Long userId = jwtTokenProvider.getUserId(claims);
        Long sessionId = jwtTokenProvider.getSessionId(claims);
        String refreshTokenId = jwtTokenProvider.getTokenId(claims);
        if (userId == null || sessionId == null || refreshTokenId == null || refreshTokenId.isBlank()) {
            throw new BadCredentialsException("Некорректный refresh токен");
        }

        AppUser user = appUserRepository.findById(userId)
                .filter(AppUser::isEnabled)
                .orElseThrow(() -> new BadCredentialsException("Пользователь не найден или отключен"));

        UserSession oldSession = userSessionRepository.findById(sessionId)
                .orElseThrow(() -> new BadCredentialsException("Сессия не найдена"));

        if (!oldSession.getUser().getId().equals(userId)) {
            throw new BadCredentialsException("Сессия не принадлежит пользователю");
        }
        if (!oldSession.getRefreshTokenId().equals(refreshTokenId)) {
            throw new BadCredentialsException("Refresh токен не соответствует сессии");
        }
        if (oldSession.getStatus() != SessionStatus.ACTIVE) {
            throw new BadCredentialsException("Refresh токен уже использован");
        }
        if (oldSession.getExpiresAt().isBefore(LocalDateTime.now())) {
            oldSession.setStatus(SessionStatus.EXPIRED);
            oldSession.setLastUsedAt(LocalDateTime.now());
            userSessionRepository.save(oldSession);
            throw new BadCredentialsException("Срок действия refresh токена истек");
        }

        oldSession.setStatus(SessionStatus.REFRESHED);
        oldSession.setLastUsedAt(LocalDateTime.now());

        UserSession newSession = createActiveSession(user);
        oldSession.setReplacedBySessionId(newSession.getId());
        userSessionRepository.save(oldSession);

        return issueTokenPair(user, newSession);
    }

    private UserSession createActiveSession(AppUser user) {
        LocalDateTime now = LocalDateTime.now();
        UserSession session = new UserSession();
        session.setUser(user);
        session.setRefreshTokenId(UUID.randomUUID().toString());
        session.setStatus(SessionStatus.ACTIVE);
        session.setIssuedAt(now);
        session.setLastUsedAt(now);
        session.setExpiresAt(now.plusSeconds(jwtTokenProvider.getRefreshExpirationSeconds()));
        return userSessionRepository.save(session);
    }

    private TokenPairResponse issueTokenPair(AppUser user, UserSession session) {
        String accessToken = jwtTokenProvider.generateAccessToken(user, session.getId());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user, session.getRefreshTokenId(), session.getId());

        return TokenPairResponse.builder()
                .tokenType("Bearer")
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .accessExpiresInSeconds(jwtTokenProvider.getAccessExpirationSeconds())
                .refreshExpiresInSeconds(jwtTokenProvider.getRefreshExpirationSeconds())
                .build();
    }
}
