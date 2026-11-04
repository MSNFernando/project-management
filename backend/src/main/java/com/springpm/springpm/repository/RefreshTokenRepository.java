package com.springpm.springpm.repository;

import com.springpm.springpm.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    Optional<RefreshToken> findByTokenHash(String tokenHash);

    /** Revoke every live refresh token a user has (sign out everywhere, or a reused token). */
    @Modifying
    @Query("UPDATE RefreshToken t SET t.isRevoked = true WHERE t.user.id = :userId AND t.isRevoked = false")
    int revokeAllForUser(@Param("userId") UUID userId);

    /** Housekeeping: drop a user's tokens that expired before the given time. */
    @Modifying
    @Query("DELETE FROM RefreshToken t WHERE t.user.id = :userId AND t.expiresAt < :before")
    int deleteExpiredForUser(@Param("userId") UUID userId, @Param("before") LocalDateTime before);
}
