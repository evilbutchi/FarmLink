package com.farmlink.service;

import com.farmlink.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * Delivers the reset link. When SMTP is configured (SPRING_MAIL_HOST etc.) it is emailed to the
 * account owner. When it is not, the link is printed to the server console so the feature can still
 * be demonstrated locally. Failures never reach the caller, so the API cannot be used to probe accounts.
 */
@Component
public class PasswordResetMailer {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetMailer.class);

    private final ObjectProvider<JavaMailSender> mailSender;
    private final String from;
    private final String frontendUrl;

    public PasswordResetMailer(ObjectProvider<JavaMailSender> mailSender,
                               @Value("${app.mail.from}") String from,
                               @Value("${app.frontend-url}") String frontendUrl) {
        this.mailSender = mailSender;
        this.from = from;
        this.frontendUrl = frontendUrl;
    }

    public void sendResetLink(User user, String rawToken, Duration ttl) {
        String base = frontendUrl.replaceAll("/+$", "");
        String link = base + "/reset-password?token=" + URLEncoder.encode(rawToken, StandardCharsets.UTF_8);

        JavaMailSender sender = mailSender.getIfAvailable();
        if (sender == null) {
            log.warn("SMTP is not configured, so no email was sent. DEV MODE reset link for {}: {}",
                    user.getEmail(), link);
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(user.getEmail());
            message.setSubject("Reset your FarmLink password");
            message.setText("Hello " + user.getFullName() + ",\n\n"
                    + "We received a request to reset your FarmLink password. "
                    + "Open the link below to choose a new one. It can be used once and expires in "
                    + ttl.toMinutes() + " minutes.\n\n"
                    + link + "\n\n"
                    + "If you did not request this, you can ignore this email. Your password will not change.");
            sender.send(message);
            log.info("Password reset email sent to {}", user.getEmail());
        } catch (MailException ex) {
            log.error("Could not send password reset email to {}: {}", user.getEmail(), ex.getMessage());
        }
    }
}
