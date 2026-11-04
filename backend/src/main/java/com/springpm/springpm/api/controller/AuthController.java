package com.springpm.springpm.api.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.springpm.springpm.api.dto.request.LoginRequest;
import com.springpm.springpm.api.dto.request.RegisterRequest;
import com.springpm.springpm.api.dto.response.AuthResponse;
import com.springpm.springpm.api.dto.response.UserDTO;
import com.springpm.springpm.service.AuthService;
import com.springpm.springpm.exception.UnauthorizedException;
import com.springpm.springpm.util.ApiResponse;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * Register a new user
     * POST /api/auth/register
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@RequestBody RegisterRequest request) {
        log.info("User registration request for email: {}", request.getEmail());
        try {
            UserDTO userDTO = authService.register(request);
            
            // For now, return a basic auth response without tokens on registration
            AuthResponse response = AuthResponse.builder()
                    .tokenType("Bearer")
                    .user(userDTO)
                    .build();
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage(), "REGISTRATION_FAILED"));
        }
    }

    /**
     * Login user
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@RequestBody LoginRequest request) {
        log.info("User login request for email: {}", request.getEmail());
        try {
            AuthResponse authResp = authService.login(request);
            return ResponseEntity.ok(ApiResponse.success(authResp));
        } catch (UnauthorizedException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(e.getMessage(), "LOGIN_FAILED"));
        } catch (IllegalArgumentException e) {
            // e.g. a missing password; don't echo the library's message
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Invalid email or password", "LOGIN_FAILED"));
        }
    }

    /**
     * Refresh JWT token
     * POST /api/auth/refresh
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(@RequestBody TokenRefreshRequest request) {
        log.info("Token refresh request");
        try {
            AuthResponse refreshResp = authService.refreshToken(request.getToken());
            return ResponseEntity.ok(ApiResponse.success(refreshResp));
        } catch (UnauthorizedException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(e.getMessage(), "TOKEN_REFRESH_FAILED"));
        }
    }

    /**
     * Sign out: revoke the refresh token so it can't be used again.
     * POST /auth/logout  { "token": "<refresh token>" }
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody(required = false) TokenRefreshRequest request) {
        authService.logout(request != null ? request.getToken() : null);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get current user profile
     * GET /api/auth/me
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserDTO>> getCurrentUser() {
        try {
            UserDTO userDTO = authService.getCurrentUser();
            return ResponseEntity.ok(ApiResponse.success(userDTO));
        } catch (UnauthorizedException | IllegalStateException e) {
            // IllegalStateException: no signed-in user on this request
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Sign in to continue.", "UNAUTHORIZED"));
        }
    }

    // Request DTO for token refresh
    @lombok.Data
    public static class TokenRefreshRequest {
        private String token;
    }
}
