package com.recruting.demo.security.service;

import com.recruting.demo.security.entity.AppUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Component
public class JwtTokenProvider {

    @Value("${security.jwt.access-secret}")
    private String accessSecret;

    @Value("${security.jwt.refresh-secret}")
    private String refreshSecret;

    @Value("${security.jwt.access-expiration-minutes:15}")
    private long accessExpirationMinutes;

    @Value("${security.jwt.refresh-expiration-days:7}")
    private long refreshExpirationDays;

    private SecretKey accessKey;
    private SecretKey refreshKey;

    @PostConstruct
    void init() {
        byte[] accessBytes = Decoders.BASE64.decode(accessSecret);
        byte[] refreshBytes = Decoders.BASE64.decode(refreshSecret);
        this.accessKey = Keys.hmacShaKeyFor(accessBytes);
        this.refreshKey = Keys.hmacShaKeyFor(refreshBytes);
    }

    public String generateAccessToken(AppUser user, Long sessionId) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(Duration.ofMinutes(accessExpirationMinutes));
        return Jwts.builder()
                .subject(user.getUsername())
                .id(UUID.randomUUID().toString())
                .claim("userId", user.getId())
                .claim("email", user.getEmail())
                .claim("roles", user.getRoles().stream().map(role -> role.getName().name()).toList())
                .claim("tokenType", TokenType.ACCESS.name())
                .claim("sessionId", sessionId)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(accessKey)
                .compact();
    }

    public String generateRefreshToken(AppUser user, String refreshTokenId, Long sessionId) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(Duration.ofDays(refreshExpirationDays));
        return Jwts.builder()
                .subject(user.getUsername())
                .id(refreshTokenId)
                .claim("userId", user.getId())
                .claim("email", user.getEmail())
                .claim("roles", user.getRoles().stream().map(role -> role.getName().name()).toList())
                .claim("tokenType", TokenType.REFRESH.name())
                .claim("sessionId", sessionId)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(refreshKey)
                .compact();
    }

    public Claims parseAndValidate(String token, TokenType expectedType) {
        try {
            SecretKey key = expectedType == TokenType.REFRESH ? refreshKey : accessKey;
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String tokenType = claims.get("tokenType", String.class);
            if (tokenType == null || !expectedType.name().equals(tokenType)) {
                throw new BadCredentialsException("Неверный тип токена");
            }
            return claims;
        } catch (SecurityException | IllegalArgumentException e) {
            throw new BadCredentialsException("Некорректный токен");
        } catch (io.jsonwebtoken.JwtException e) {
            throw new BadCredentialsException("Токен недействителен или истек");
        }
    }

    public String getUsername(String token) {
        return parseAndValidate(token, TokenType.ACCESS).getSubject();
    }

    public Long getUserId(Claims claims) {
        Number raw = claims.get("userId", Number.class);
        return raw == null ? null : raw.longValue();
    }

    public Long getSessionId(Claims claims) {
        Number raw = claims.get("sessionId", Number.class);
        return raw == null ? null : raw.longValue();
    }

    public String getTokenId(Claims claims) {
        return claims.getId();
    }

    @SuppressWarnings("unchecked")
    public List<String> getRoles(Claims claims) {
        Object raw = claims.get("roles");
        if (raw instanceof List<?> list) {
            return list.stream().map(String::valueOf).toList();
        }
        return List.of();
    }

    public long getAccessExpirationSeconds() {
        return Duration.ofMinutes(accessExpirationMinutes).toSeconds();
    }

    public long getRefreshExpirationSeconds() {
        return Duration.ofDays(refreshExpirationDays).toSeconds();
    }
}
