package com.farmlink.service;

import com.farmlink.dto.ResetPasswordRequest;
import com.farmlink.exception.FieldValidationException;
import com.farmlink.exception.InvalidResetTokenException;
import com.farmlink.model.PasswordResetToken;
import com.farmlink.model.User;
import com.farmlink.repository.PasswordResetTokenRepository;
import com.farmlink.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Map;
import java.util.Optional;

@Service
public class PasswordResetService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetMailer mailer;
    private final Duration ttl;
    private final Duration cooldown;

    public PasswordResetService(UserRepository userRepository,
                                PasswordResetTokenRepository tokenRepository,
                                PasswordEncoder passwordEncoder,
                                PasswordResetMailer mailer,
                                @Value("${app.reset-token.ttl-minutes}") long ttlMinutes,
                                @Value("${app.reset-token.cooldown-seconds}") long cooldownSeconds) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailer = mailer;
        this.ttl = Duration.ofMinutes(ttlMinutes);
        this.cooldown = Duration.ofSeconds(cooldownSeconds);
    }

    /**
     * Starts a reset. Always completes silently, whether or not the email belongs to an account,
     * so the response cannot be used to discover which emails are registered.
     */
    @Transactional
    public void requestReset(String rawEmail) {
        Instant now = Instant.now();
        tokenRepository.deleteExpiredBefore(now.minus(Duration.ofDays(1)));

        Optional<User> found = userRepository.findByEmail(UserService.normalizeEmail(rawEmail));
        if (found.isEmpty() || !found.get().isEnabled()) {
            return;
        }
        User user = found.get();

        // Basic abuse protection: at most one new link per cooldown window per account.
        Optional<PasswordResetToken> last = tokenRepository.findTopByUserOrderByCreatedAtDesc(user);
        if (last.isPresent() && last.get().getCreatedAt().plus(cooldown).isAfter(now)) {
            return;
        }

        // Only the newest link works.
        tokenRepository.invalidateAllFor(user, now);

        String rawToken = generateToken();
        PasswordResetToken token = new PasswordResetToken();
        token.setUser(user);
        token.setTokenHash(hash(rawToken));
        token.setExpiresAt(now.plus(ttl));
        tokenRepository.save(token);

        mailer.sendResetLink(user, rawToken, ttl);
    }

    @Transactional(readOnly = true)
    public boolean isTokenValid(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            return false;
        }
        Instant now = Instant.now();
        return tokenRepository.findByTokenHash(hash(rawToken.trim()))
                .filter(t -> t.getUsedAt() == null && t.getExpiresAt().isAfter(now))
                .isPresent();
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        if (!request.newPassword().equals(request.confirmPassword())) {
            throw new FieldValidationException(Map.of("confirmPassword", "Passwords do not match"));
        }

        Instant now = Instant.now();
        String tokenHash = hash(request.token().trim());

        PasswordResetToken token = tokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(InvalidResetTokenException::new);
        Long userId = token.getUser().getId();

        // Single use and expiry are enforced atomically by this UPDATE.
        if (tokenRepository.consume(tokenHash, now) != 1) {
            throw new InvalidResetTokenException();
        }

        User user = userRepository.findById(userId).orElseThrow(InvalidResetTokenException::new);
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        user.setPasswordChangedAt(now);
        userRepository.save(user);

        // Any other outstanding links for this account are now useless.
        tokenRepository.invalidateAllFor(user, now);
    }

    private static String generateToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    static String hash(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(rawToken.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
