package com.springpm.springpm.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.nio.charset.StandardCharsets;
import java.util.*;
import javax.crypto.SecretKey;

@Slf4j
@Component
public class JwtTokenProvider {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration}")
    private long jwtExpirationMs;

    @Value("${app.jwt.refresh-expiration}")
    private long refreshTokenExpirationMs;

    /**
     * Generate JWT token for user
     */
    public String generateToken(String userId, String email, String workspaceId, String role) {
        return createToken(userId, email, workspaceId, role, jwtExpirationMs);
    }

    /**
     * Generate refresh token
     */
    public String generateRefreshToken(String userId, String email) {
        return createToken(userId, email, null, null, refreshTokenExpirationMs);
    }

    /**
     * Create JWT token with claims
     */
    private String createToken(String userId, String email, String workspaceId, String role, long expirationTime) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("email", email);
        if (workspaceId != null) {
            claims.put("workspaceId", workspaceId);
        }
        if (role != null) {
            claims.put("role", role);
        }

        return Jwts.builder()
                .claims(claims)
                .subject(email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Get signing key from secret
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Extract claims from token
     */
    public Claims extractClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException e) {
            log.error("Error parsing JWT token: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Validate token
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.debug("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.debug("JWT token is unsupported: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.debug("Invalid JWT token: {}", e.getMessage());
        } catch (SignatureException e) {
            log.debug("JWT signature validation failed: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.debug("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }

    /**
     * Extract user ID from token
     */
    public String getUserIdFromToken(String token) {
        Claims claims = extractClaims(token);
        return claims.get("userId", String.class);
    }

    /**
     * Extract email from token
     */
    public String getEmailFromToken(String token) {
        Claims claims = extractClaims(token);
        return claims.getSubject();
    }

    /**
     * Extract workspace ID from token
     */
    public String getWorkspaceIdFromToken(String token) {
        Claims claims = extractClaims(token);
        return claims.get("workspaceId", String.class);
    }

    /**
     * Extract role from token
     */
    public String getRoleFromToken(String token) {
        Claims claims = extractClaims(token);
        return claims.get("role", String.class);
    }
}
