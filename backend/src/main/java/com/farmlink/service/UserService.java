package com.farmlink.service;

import com.farmlink.dto.ChangePasswordRequest;
import com.farmlink.dto.RegisterRequest;
import com.farmlink.dto.UpdateProfileRequest;
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

import java.time.Instant;
import java.util.LinkedHashMap;
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

    /**
     * Updates the profile of the given (already authenticated) user. The target account is always
     * the one identified by the session, never by a client-supplied id.
     */
    @Transactional
    public UserResponse updateProfile(String email, UpdateProfileRequest req) {
        User user = findAuthenticatedUser(email);

        Map<String, String> errors = new LinkedHashMap<>();
        if (user.getRole() == Role.FARMER) {
            if (isBlank(req.farmName())) {
                errors.put("farmName", "Farm name is required for farmers");
            }
            if (isBlank(req.farmLocation())) {
                errors.put("farmLocation", "Farm location is required for farmers");
            }
        } else if (req.buyerType() == null) {
            errors.put("buyerType", "Select a buyer type");
        }
        if (!errors.isEmpty()) {
            throw new FieldValidationException(errors);
        }

        user.setFullName(req.fullName().trim());
        user.setPhone(blankToNull(req.phone()));
        if (user.getRole() == Role.FARMER) {
            user.setFarmName(req.farmName().trim());
            user.setFarmLocation(req.farmLocation().trim());
        } else {
            user.setBuyerType(req.buyerType());
            user.setDeliveryAddress(blankToNull(req.deliveryAddress()));
        }
        return UserResponse.from(userRepository.save(user));
    }

    /** Changes the password of the authenticated user after verifying the current one. */
    @Transactional
    public void changePassword(String email, ChangePasswordRequest req) {
        User user = findAuthenticatedUser(email);

        Map<String, String> errors = new LinkedHashMap<>();
        if (!passwordEncoder.matches(req.currentPassword(), user.getPasswordHash())) {
            errors.put("currentPassword", "Current password is incorrect");
        }
        if (!req.newPassword().equals(req.confirmNewPassword())) {
            errors.put("confirmNewPassword", "Passwords do not match");
        } else if (req.newPassword().equals(req.currentPassword())) {
            errors.put("newPassword", "New password must be different from the current password");
        }
        if (!errors.isEmpty()) {
            throw new FieldValidationException(errors);
        }

        // The old hash is overwritten, so the previous password can no longer authenticate.
        user.setPasswordHash(passwordEncoder.encode(req.newPassword()));
        user.setPasswordChangedAt(Instant.now());
        userRepository.save(user);
    }

    private User findAuthenticatedUser(String email) {
        return userRepository.findByEmail(normalizeEmail(email))
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private static String blankToNull(String s) {
        return isBlank(s) ? null : s.trim();
    }
}
