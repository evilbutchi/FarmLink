package com.farmlink.controller;

import com.farmlink.dto.ChangePasswordRequest;
import com.farmlink.dto.MessageResponse;
import com.farmlink.dto.UpdateProfileRequest;
import com.farmlink.dto.UserResponse;
import com.farmlink.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * "My account" endpoints. None of them accepts a user id: every operation applies to the
 * authenticated user taken from the session, so one user can never read or change another's profile.
 */
@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final UserService userService;

    public ProfileController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public UserResponse getProfile(Authentication authentication) {
        return userService.getByEmail(authentication.getName());
    }

    @PutMapping
    public UserResponse updateProfile(@Valid @RequestBody UpdateProfileRequest request,
                                      Authentication authentication) {
        return userService.updateProfile(authentication.getName(), request);
    }

    @PostMapping("/password")
    public MessageResponse changePassword(@Valid @RequestBody ChangePasswordRequest request,
                                          Authentication authentication) {
        userService.changePassword(authentication.getName(), request);
        return new MessageResponse("Your password has been changed.");
    }
}
