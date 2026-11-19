package com.farmlink.config;

import com.farmlink.model.Role;
import com.farmlink.model.User;
import com.farmlink.repository.UserRepository;
import com.farmlink.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Creates the first administrator account in the database on startup, using
 * credentials from configuration (ADMIN_EMAIL / ADMIN_PASSWORD). The password is
 * BCrypt-hashed before storage; nothing is hardcoded in source.
 */
@Component
public class AdminSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminSeeder.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminEmail;
    private final String adminName;
    private final String adminPassword;

    public AdminSeeder(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       @Value("${app.admin.email}") String adminEmail,
                       @Value("${app.admin.full-name}") String adminName,
                       @Value("${app.admin.password}") String adminPassword) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminEmail = adminEmail;
        this.adminName = adminName;
        this.adminPassword = adminPassword;
    }

    @Override
    public void run(String... args) {
        if (adminPassword == null || adminPassword.isBlank()) {
            log.warn("ADMIN_PASSWORD is not set; skipping admin account creation.");
            return;
        }
        String email = UserService.normalizeEmail(adminEmail);
        if (userRepository.existsByEmail(email)) {
            return;
        }
        User admin = new User();
        admin.setFullName(adminName);
        admin.setEmail(email);
        admin.setPasswordHash(passwordEncoder.encode(adminPassword));
        admin.setRole(Role.ADMIN);
        userRepository.save(admin);
        log.info("Created administrator account {}", email);
    }
}
