package com.saleorigano.service;

import com.saleorigano.dto.AuthResponse;
import com.saleorigano.dto.ChangePasswordRequest;
import com.saleorigano.dto.LoginRequest;
import com.saleorigano.model.AppUser;
import com.saleorigano.model.RefreshToken;
import com.saleorigano.repository.RefreshTokenRepository;
import com.saleorigano.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
public class AuthService {
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, RefreshTokenRepository refreshTokenRepository,
                       JwtService jwtService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        AppUser user = userRepository.findByUsername(request.getUsername())
            .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.getEnabled()) {
            throw new RuntimeException("User account is disabled");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        String accessToken = jwtService.generateAccessToken(user.getUsername(), user.getId(), user.getRole().toString());
        RefreshToken refreshToken = createRefreshToken(user);

        return AuthResponse.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken.getToken())
            .username(user.getUsername())
            .role(user.getRole().toString())
            .userId(user.getId())
            .build();
    }

    @Transactional
    public AuthResponse refresh(String refreshTokenString) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenString)
            .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        if (refreshToken.getRevoked() || refreshToken.isExpired()) {
            throw new RuntimeException("Refresh token is revoked or expired");
        }

        AppUser user = refreshToken.getUser();
        if (!user.getEnabled()) {
            throw new RuntimeException("User account is disabled");
        }

        // Revoke old refresh token and create new one (token rotation)
        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);

        String accessToken = jwtService.generateAccessToken(user.getUsername(), user.getId(), user.getRole().toString());
        RefreshToken newRefreshToken = createRefreshToken(user);

        return AuthResponse.builder()
            .accessToken(accessToken)
            .refreshToken(newRefreshToken.getToken())
            .username(user.getUsername())
            .role(user.getRole().toString())
            .userId(user.getId())
            .build();
    }

    @Transactional
    public void logout(String username) {
        AppUser user = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));
        refreshTokenRepository.revokeAllByUser(user);
    }

    @Transactional
    public void changePassword(String username, ChangePasswordRequest request) {
        AppUser user = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Revoke all refresh tokens to force re-login
        refreshTokenRepository.revokeAllByUser(user);
    }

    private RefreshToken createRefreshToken(AppUser user) {
        // Get the expiration time from properties - refresh token is valid for 7 days
        long expirationMs = 604800000L; // 7 days in milliseconds
        LocalDateTime expiresAt = LocalDateTime.now().plusNanos(expirationMs * 1_000_000L);

        RefreshToken refreshToken = RefreshToken.builder()
            .user(user)
            .expiresAt(expiresAt)
            .revoked(false)
            .build();

        return refreshTokenRepository.save(refreshToken);
    }
}
