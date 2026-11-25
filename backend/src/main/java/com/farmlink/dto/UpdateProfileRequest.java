package com.farmlink.dto;

import com.farmlink.model.BuyerType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Editable profile fields only. Email, role and id are deliberately absent, so a client
 * cannot change them (or anyone else's account) through this request.
 */
public record UpdateProfileRequest(
        @NotBlank(message = "Full name is required")
        @Size(min = 2, max = 100, message = "Full name must be 2 to 100 characters")
        String fullName,

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
