package com.comandapro.service;

import com.comandapro.dto.CreateUserRequest;
import com.comandapro.dto.UserDTO;
import com.comandapro.model.AppUser;
import com.comandapro.model.UserRole;
import com.comandapro.repository.RefreshTokenRepository;
import com.comandapro.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class UserService {
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, RefreshTokenRepository refreshTokenRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
            .map(this::toDTO)
            .toList();
    }

    @Transactional
    public UserDTO createUser(CreateUserRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        AppUser user = AppUser.builder()
            .username(request.getUsername())
            .password(passwordEncoder.encode(request.getPassword()))
            .role(UserRole.valueOf(request.getRole()))
            .enabled(true)
            .build();

        AppUser savedUser = userRepository.save(user);
        return toDTO(savedUser);
    }

    @Transactional
    public UserDTO toggleEnabled(Long userId) {
        AppUser user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        // Prevent disabling the last admin
        if (user.getRole() == UserRole.ROLE_ADMIN && user.getEnabled()) {
            long adminCount = userRepository.findAll().stream()
                .filter(u -> u.getRole() == UserRole.ROLE_ADMIN && u.getEnabled())
                .count();
            if (adminCount == 1) {
                throw new RuntimeException("Cannot disable the last admin user");
            }
        }

        user.setEnabled(!user.getEnabled());
        AppUser updated = userRepository.save(user);

        // Revoke all tokens if disabling user
        if (!updated.getEnabled()) {
            refreshTokenRepository.revokeAllByUser(user);
        }

        return toDTO(updated);
    }

    @Transactional
    public void deleteUser(Long userId) {
        AppUser user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        // Prevent deleting the last admin
        if (user.getRole() == UserRole.ROLE_ADMIN) {
            long adminCount = userRepository.findAll().stream()
                .filter(u -> u.getRole() == UserRole.ROLE_ADMIN)
                .count();
            if (adminCount == 1) {
                throw new RuntimeException("Cannot delete the last admin user");
            }
        }

        refreshTokenRepository.revokeAllByUser(user);
        userRepository.deleteById(userId);
    }

    @Transactional
    public void resetPassword(Long userId, String newPassword) {
        AppUser user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Revoke all tokens after password reset
        refreshTokenRepository.revokeAllByUser(user);
    }

    private UserDTO toDTO(AppUser user) {
        return UserDTO.builder()
            .id(user.getId())
            .username(user.getUsername())
            .role(user.getRole().toString())
            .enabled(user.getEnabled())
            .createdAt(user.getCreatedAt())
            .build();
    }
}
