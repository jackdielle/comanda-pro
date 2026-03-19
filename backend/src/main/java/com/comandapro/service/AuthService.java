package com.comandapro.service;

import com.comandapro.dto.AuthResponse;
import com.comandapro.dto.ChangePasswordRequest;
import com.comandapro.dto.LoginRequest;
import com.comandapro.model.AppUser;
import com.comandapro.model.RefreshToken;
import com.comandapro.repository.RefreshTokenRepository;
import com.comandapro.repository.UserRepository;
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
        log.info("=== LOGIN ATTEMPT === Username: {}", request.getUsername());

        log.debug("Step 1: Looking up user in database");
        AppUser user = userRepository.findByUsername(request.getUsername())
            .orElseThrow(() -> {
                log.error("User not found: {}", request.getUsername());
                return new RuntimeException("User not found");
            });
        log.info("Step 2: User found - ID: {}, Role: {}", user.getId(), user.getRole());

        if (!user.getEnabled()) {
            log.error("User account disabled: {}", request.getUsername());
            throw new RuntimeException("User account is disabled");
        }
        log.debug("Step 3: User account is enabled");

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.error("Invalid password for user: {}", request.getUsername());
            throw new RuntimeException("Invalid credentials");
        }
        log.debug("Step 4: Password verified");

        log.debug("Step 5: Generating JWT tokens");
        String accessToken = jwtService.generateAccessToken(user.getUsername(), user.getId(), user.getRole().toString());
        RefreshToken refreshToken = createRefreshToken(user);
        log.info("Step 6: Tokens generated successfully");

        AuthResponse response = AuthResponse.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken.getToken())
            .username(user.getUsername())
            .role(user.getRole().toString())
            .userId(user.getId())
            .build();

        log.info("=== LOGIN SUCCESS === User: {} logged in", request.getUsername());
        return response;
    }

    @Transactional
    public AuthResponse refresh(String refreshTokenString) {
        log.info("=== TOKEN REFRESH ATTEMPT ===");
        log.debug("Step 1: Looking up refresh token");

        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenString)
            .orElseThrow(() -> {
                log.error("Refresh token not found in database");
                return new RuntimeException("Invalid refresh token");
            });
        log.debug("Step 2: Refresh token found");

        if (refreshToken.getRevoked() || refreshToken.isExpired()) {
            log.error("Refresh token is revoked or expired - Revoked: {}, Expired: {}",
                refreshToken.getRevoked(), refreshToken.isExpired());
            throw new RuntimeException("Refresh token is revoked or expired");
        }
        log.debug("Step 3: Refresh token is valid");

        AppUser user = refreshToken.getUser();
        log.debug("Step 4: User found - Username: {}", user.getUsername());

        if (!user.getEnabled()) {
            log.error("User account disabled: {}", user.getUsername());
            throw new RuntimeException("User account is disabled");
        }

        // Revoke old refresh token and create new one (token rotation)
        log.debug("Step 5: Revoking old refresh token");
        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);

        log.debug("Step 6: Generating new tokens");
        String accessToken = jwtService.generateAccessToken(user.getUsername(), user.getId(), user.getRole().toString());
        RefreshToken newRefreshToken = createRefreshToken(user);

        log.info("=== TOKEN REFRESH SUCCESS === User: {}", user.getUsername());
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
