package com.farmlink.dto;

import com.farmlink.model.BuyerType;
import com.farmlink.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Full name is required")
        @Size(min = 2, max = 100, message = "Full name must be 2 to 100 characters")
        String fullName,

        @NotBlank(message = "Email is required")
        @Email(message = "Enter a valid email address")
        @Size(max = 150, message = "Email is too long")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 72, message = "Password must be 8 to 72 characters")
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$",
                message = "Password must contain at least one letter and one number")
        String password,

        @NotBlank(message = "Please confirm your password")
        String confirmPassword,

        @NotNull(message = "Select a role")
        Role role,

        @Pattern(regexp = "^$|^[0-9+()\\-\\s]{7,20}$", message = "Enter a valid phone number")
        String phone,

        @Size(max = 120, message = "Farm name is too long")
        String farmName,

        @Size(max = 200, message = "Farm location is too long")
        String farmLocation,

        BuyerType buyerType,

        @Size(max = 250, message = "Address is too long")
        String deliveryAddress
) {
}
