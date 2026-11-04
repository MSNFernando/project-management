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
import com.springpm.springpm.entity.RefreshToken;
import com.springpm.springpm.entity.User;
import com.springpm.springpm.exception.UnauthorizedException;
import com.springpm.springpm.repository.RefreshTokenRepository;
import com.springpm.springpm.repository.UserRepository;
import com.springpm.springpm.security.JwtTokenProvider;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;

@Slf4j
@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    private static final SecureRandom RANDOM = new SecureRandom();

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
    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid email or password");
        }

        if (!user.getIsActive()) {
            throw new UnauthorizedException("User account is inactive");
        }

        refreshTokenRepository.deleteExpiredForUser(user.getId(), LocalDateTime.now());
        return issueTokens(user);
    }

    /**
     * A short-lived access token (JWT) plus a new refresh token.
     * The refresh token is a random string; only its SHA-256 hash is stored,
     * so it can't be used as an access token and a database leak doesn't expose it.
     */
    private AuthResponse issueTokens(User user) {
        String accessToken = jwtTokenProvider.generateToken(
                user.getId().toString(),
                user.getEmail(),
                null, // workspaceId not set on login
                null  // role not set on login
        );

        byte[] raw = new byte[32];
        RANDOM.nextBytes(raw);
        String refreshToken = Base64.getUrlEncoder().withoutPadding().encodeToString(raw);
        refreshTokenRepository.save(RefreshToken.builder()
                .user(user)
                .tokenHash(hash(refreshToken))
                .expiresAt(LocalDateTime.now().plusNanos(jwtTokenProvider.getRefreshTokenExpirationMs() * 1_000_000L))
                .build());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getAccessTokenExpirySeconds())
                .user(toUserDTO(user))
                .build();
    }

    private static String hash(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    /**
     * Convert User entity to UserDTO
     */
    private UserDTO toUserDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .fullName(user.getName())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    /**
     * Exchange a refresh token for a new access token and a new refresh token.
     * Each refresh token works once. If a used one comes back, someone may have a copy,
     * so every refresh token the user has is revoked and they must sign in again.
     * noRollbackFor keeps that revocation when the request is then refused.
     */
    @Transactional(noRollbackFor = UnauthorizedException.class)
    public AuthResponse refreshToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new UnauthorizedException("Invalid refresh token");
        }
        RefreshToken stored = refreshTokenRepository.findByTokenHash(hash(refreshToken))
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));
        User user = stored.getUser();

        if (Boolean.TRUE.equals(stored.getIsRevoked())) {
            int revoked = refreshTokenRepository.revokeAllForUser(user.getId());
            log.warn("Reused refresh token for user {}; revoked {} live refresh tokens", user.getId(), revoked);
            throw new UnauthorizedException("Refresh token already used. Sign in again.");
        }
        if (stored.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new UnauthorizedException("Refresh token expired. Sign in again.");
        }
        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new UnauthorizedException("User account is inactive");
        }

        stored.setIsRevoked(true);
        refreshTokenRepository.save(stored);
        return issueTokens(user);
    }

    /**
     * Sign out: revoke the given refresh token. Unknown or already-revoked tokens are ignored,
     * so signing out never fails.
     */
    @Transactional
    public void logout(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return;
        }
        refreshTokenRepository.findByTokenHash(hash(refreshToken)).ifPresent(t -> {
            t.setIsRevoked(true);
            refreshTokenRepository.save(t);
        });
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
