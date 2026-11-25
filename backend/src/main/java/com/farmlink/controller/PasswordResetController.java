package com.farmlink.controller;

import com.farmlink.dto.ForgotPasswordRequest;
import com.farmlink.dto.MessageResponse;
import com.farmlink.dto.ResetPasswordRequest;
import com.farmlink.dto.TokenCheckRequest;
import com.farmlink.service.PasswordResetService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** Public endpoints for the "Forgot password" flow (the user is not logged in). */
@RestController
@RequestMapping("/api/auth")
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    public PasswordResetController(PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;
    }

    @PostMapping("/forgot-password")
    public MessageResponse forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        passwordResetService.requestReset(request.email());
        // Same answer whether or not the account exists.
        return new MessageResponse(
                "If an account exists for that email, a password reset link has been sent.");
    }

    @PostMapping("/reset-password/validate")
    public Map<String, Boolean> validateToken(@Valid @RequestBody TokenCheckRequest request) {
        return Map.of("valid", passwordResetService.isTokenValid(request.token()));
    }

    @PostMapping("/reset-password")
    public MessageResponse resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(request);
        return new MessageResponse("Your password has been reset. You can now log in.");
    }
}
