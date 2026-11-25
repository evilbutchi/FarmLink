package com.farmlink.controller;

import com.farmlink.dto.ProfileDashboardResponse;
import com.farmlink.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Role-specific dashboard endpoints. Access is restricted per role in SecurityConfig
 * (/api/farmer/** -> FARMER, /api/buyer/** -> BUYER).
 */
@RestController
@RequestMapping("/api")
public class DashboardController {

    private final UserService userService;

    public DashboardController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/farmer/dashboard")
    public ProfileDashboardResponse farmerDashboard(Authentication authentication) {
        var profile = userService.getByEmail(authentication.getName());
        return new ProfileDashboardResponse("Welcome back, " + profile.fullName(), profile);
    }

    @GetMapping("/buyer/dashboard")
    public ProfileDashboardResponse buyerDashboard(Authentication authentication) {
        var profile = userService.getByEmail(authentication.getName());
        return new ProfileDashboardResponse("Welcome back, " + profile.fullName(), profile);
    }
}
