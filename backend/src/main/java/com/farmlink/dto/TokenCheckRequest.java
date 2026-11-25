package com.farmlink.dto;

import jakarta.validation.constraints.NotBlank;

public record TokenCheckRequest(
        @NotBlank(message = "Reset token is missing")
        String token
) {
}
