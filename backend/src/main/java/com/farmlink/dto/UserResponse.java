package com.farmlink.dto;

import com.farmlink.model.BuyerType;
import com.farmlink.model.Role;
import com.farmlink.model.User;

import java.time.Instant;

/** Safe view of a user. Never includes the password hash. */
public record UserResponse(
        Long id,
        String fullName,
        String email,
        Role role,
        String phone,
        String farmName,
        String farmLocation,
        BuyerType buyerType,
        String deliveryAddress,
        boolean enabled,
        Instant createdAt
) {
    public static UserResponse from(User u) {
        return new UserResponse(
                u.getId(),
                u.getFullName(),
                u.getEmail(),
                u.getRole(),
                u.getPhone(),
                u.getFarmName(),
                u.getFarmLocation(),
                u.getBuyerType(),
                u.getDeliveryAddress(),
                u.isEnabled(),
                u.getCreatedAt());
    }
}
