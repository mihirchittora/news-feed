package com.newsplatform.auth.controller;

import com.newsplatform.auth.dto.LoginRequest;
import com.newsplatform.auth.dto.LoginResponse;
import com.newsplatform.auth.dto.RegisterRequest;
import com.newsplatform.auth.service.AuthService;
import com.newsplatform.user.dto.UserResponse;
import com.newsplatform.rbac.dto.StaffSetupRequest;
import com.newsplatform.rbac.service.StaffService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication")
public class AuthController {

    private final AuthService authService;
    private final StaffService staffService;

    public AuthController(AuthService authService, StaffService staffService) {
        this.authService = authService;
        this.staffService = staffService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a user account")
    public UserResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate a user")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/staff-setup")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Complete staff account setup")
    public void completeStaffSetup(@Valid @RequestBody StaffSetupRequest request) {
        staffService.completeSetup(request);
    }
}
