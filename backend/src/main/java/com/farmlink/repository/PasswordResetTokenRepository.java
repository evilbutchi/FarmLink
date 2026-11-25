package com.farmlink.repository;

import com.farmlink.model.PasswordResetToken;
import com.farmlink.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByTokenHash(String tokenHash);

    Optional<PasswordResetToken> findTopByUserOrderByCreatedAtDesc(User user);

    /**
     * Atomically consumes a token: succeeds (returns 1) only if it is still unused and unexpired.
     * Doing this in a single UPDATE guarantees single use even if two requests race.
     */
    @Modifying(flushAutomatically = true)
    @Query("update PasswordResetToken t set t.usedAt = :now "
            + "where t.tokenHash = :hash and t.usedAt is null and t.expiresAt > :now")
    int consume(@Param("hash") String hash, @Param("now") Instant now);

    /** Invalidates every still-open token of a user (used when issuing a new one or after a reset). */
    @Modifying(flushAutomatically = true)
    @Query("update PasswordResetToken t set t.usedAt = :now where t.user = :user and t.usedAt is null")
    int invalidateAllFor(@Param("user") User user, @Param("now") Instant now);

    /** Housekeeping: removes tokens that expired long ago. */
    @Modifying
    @Query("delete from PasswordResetToken t where t.expiresAt < :cutoff")
    int deleteExpiredBefore(@Param("cutoff") Instant cutoff);
}
