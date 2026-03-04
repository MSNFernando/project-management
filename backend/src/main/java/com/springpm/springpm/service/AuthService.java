package com.springpm.springpm.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.springpm.springpm.api.dto.request.LoginRequest;
import com.springpm.springpm.api.dto.request.RegisterRequest;
import com.springpm.springpm.api.dto.response.AuthResponse;
import com.springpm.springpm.api.dto.response.UserDTO;
import com.springpm.springpm.entity.User;
import com.springpm.springpm.exception.UnauthorizedException;
import com.springpm.springpm.repository.UserRepository;
import com.springpm.springpm.security.JwtTokenProvider;
import java.time.LocalDateTime;

@Slf4j
@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    /**
     * Register a new user
     */
    @Transactional
    public UserDTO register(RegisterRequest request) {
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }

        // Create new user
        User user = User.builder()
                .email(request.getEmail())
                .name(request.getName())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        user = userRepository.save(user);

        return toUserDTO(user);
    }

    /**
     * Login user and return JWT tokens
     */
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid email or password");
        }

        if (!user.getIsActive()) {
            throw new UnauthorizedException("User account is inactive");
        }

        // Generate tokens
        String accessToken = jwtTokenProvider.generateToken(
                user.getId().toString(),
                user.getEmail(),
                null, // workspaceId not set on login
                null  // role not set on login
        );

        String refreshToken = jwtTokenProvider.generateRefreshToken(
                user.getId().toString(),
                user.getEmail()
        );

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(3600) // 1 hour in seconds
                .user(toUserDTO(user))
                .build();
    }

    /**
     * Convert User entity to UserDTO
     */
    private UserDTO toUserDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    /**
     * Refresh expired access token
     */
    public AuthResponse refreshToken(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new UnauthorizedException("Invalid refresh token");
        }

        String userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
        User user = userRepository.findById(java.util.UUID.fromString(userId))
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        String newAccessToken = jwtTokenProvider.generateToken(
                user.getId().toString(),
                user.getEmail(),
                null,
                null
        );

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(3600)
                .user(toUserDTO(user))
                .build();
    }

    /**
     * Get current user details
     */
    public UserDTO getCurrentUser() {
        String userId = com.springpm.springpm.util.TenantContext.getCurrentUserIdRequired().toString();
        User user = userRepository.findById(java.util.UUID.fromString(userId))
                .orElseThrow(() -> new UnauthorizedException("User not found"));
        return toUserDTO(user);
    }
}
