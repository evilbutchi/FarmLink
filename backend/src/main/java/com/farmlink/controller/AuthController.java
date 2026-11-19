package com.farmlink.controller;

import com.farmlink.dto.LoginRequest;
import com.farmlink.dto.RegisterRequest;
import com.farmlink.dto.UserResponse;
import com.farmlink.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final SecurityContextRepository securityContextRepository =
            new HttpSessionSecurityContextRepository();
    private final SecurityContextHolderStrategy contextHolderStrategy =
            SecurityContextHolder.getContextHolderStrategy();

    public AuthController(AuthenticationManager authenticationManager, UserService userService) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
    }

    /** Lets the SPA obtain the XSRF-TOKEN cookie before its first POST. */
    @GetMapping("/csrf")
    public ResponseEntity<Void> csrf() {
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.register(request));
    }

    @PostMapping("/login")
    public UserResponse login(@Valid @RequestBody LoginRequest request,
                              HttpServletRequest httpRequest,
                              HttpServletResponse httpResponse) {
        // Throws BadCredentialsException / DisabledException (handled by GlobalExceptionHandler).
        Authentication authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(
                        UserService.normalizeEmail(request.email()), request.password()));

        // Prevent session fixation: rotate the session id if a session already exists.
        if (httpRequest.getSession(false) != null) {
            httpRequest.changeSessionId();
        }

        SecurityContext context = contextHolderStrategy.createEmptyContext();
        context.setAuthentication(authentication);
        contextHolderStrategy.setContext(context);
        securityContextRepository.saveContext(context, httpRequest, httpResponse);

        return userService.getByEmail(authentication.getName());
    }

    /** Returns the currently authenticated user, or 401 if there is no valid session. */
    @GetMapping("/me")
    public UserResponse me(Authentication authentication) {
        return userService.getByEmail(authentication.getName());
    }
}
