package com.farmlink.service;

import com.farmlink.dto.AdminDashboardResponse;
import com.farmlink.dto.RegisterRequest;
import com.farmlink.dto.UserResponse;
import com.farmlink.exception.DuplicateEmailException;
import com.farmlink.exception.FieldValidationException;
import com.farmlink.model.BuyerType;
import com.farmlink.model.Role;
import com.farmlink.model.User;
import com.farmlink.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public static String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    @Transactional
    public UserResponse register(RegisterRequest req) {
        Map<String, String> errors = new LinkedHashMap<>();

        // Admins can never self-register; they are provisioned by the bootstrap seeder.
        if (req.role() != Role.FARMER && req.role() != Role.BUYER) {
            errors.put("role", "Choose Farmer or Buyer");
        }
        if (!req.password().equals(req.confirmPassword())) {
            errors.put("confirmPassword", "Passwords do not match");
        }
        if (req.role() == Role.FARMER) {
            if (isBlank(req.farmName())) {
                errors.put("farmName", "Farm name is required for farmers");
            }
            if (isBlank(req.farmLocation())) {
                errors.put("farmLocation", "Farm location is required for farmers");
            }
        }
        if (req.role() == Role.BUYER && req.buyerType() == null) {
            errors.put("buyerType", "Select a buyer type");
        }
        if (!errors.isEmpty()) {
            throw new FieldValidationException(errors);
        }

        String email = normalizeEmail(req.email());
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateEmailException();
        }

        User user = new User();
        user.setFullName(req.fullName().trim());
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(req.password()));
        user.setRole(req.role());
        user.setPhone(blankToNull(req.phone()));

        if (req.role() == Role.FARMER) {
            user.setFarmName(req.farmName().trim());
            user.setFarmLocation(req.farmLocation().trim());
        } else {
            BuyerType type = req.buyerType();
            user.setBuyerType(type);
            user.setDeliveryAddress(blankToNull(req.deliveryAddress()));
        }

        try {
            return UserResponse.from(userRepository.saveAndFlush(user));
        } catch (DataIntegrityViolationException ex) {
            // Two simultaneous sign-ups with the same email: the unique constraint wins.
            throw new DuplicateEmailException();
        }
    }

    @Transactional(readOnly = true)
    public UserResponse getByEmail(String email) {
        return userRepository.findByEmail(normalizeEmail(email))
                .map(UserResponse::from)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    @Transactional(readOnly = true)
    public AdminDashboardResponse adminDashboard() {
        List<UserResponse> recent = userRepository.findAllByOrderByCreatedAtDesc().stream()
                .limit(5)
                .map(UserResponse::from)
                .toList();
        return new AdminDashboardResponse(
                userRepository.count(),
                userRepository.countByRole(Role.FARMER),
                userRepository.countByRole(Role.BUYER),
                userRepository.countByRole(Role.ADMIN),
                recent);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> listAllUsers() {
        return userRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(UserResponse::from)
                .toList();
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private static String blankToNull(String s) {
        return isBlank(s) ? null : s.trim();
    }
}
