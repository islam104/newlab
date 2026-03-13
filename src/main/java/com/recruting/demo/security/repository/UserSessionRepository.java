package com.recruting.demo.security.repository;

import com.recruting.demo.security.entity.SessionStatus;
import com.recruting.demo.security.entity.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserSessionRepository extends JpaRepository<UserSession, Long> {
    Optional<UserSession> findByRefreshTokenId(String refreshTokenId);

    List<UserSession> findAllByUserIdAndStatus(Long userId, SessionStatus status);
}
