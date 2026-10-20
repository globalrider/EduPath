package com.example.edupath.demo.controller;

import com.example.edupath.demo.dto.ApiResponse;
import com.example.edupath.demo.dto.AuthRequest;
import com.example.edupath.demo.dto.AuthResponse;
import com.example.edupath.demo.dto.UserProfileDto;
import com.example.edupath.demo.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * User Registration
     * Supports JUNIOR, SENIOR, ALUMNI, ADMIN roles
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody AuthRequest request) {
        AuthResponse response = authService.register(request);
        return new ResponseEntity<>(
            ApiResponse.success("Account registered successfully. JWT token issued.", response),
            HttpStatus.CREATED
        );
    }

    /**
     * User Login
     * Authenticates with email and password, returns Bearer JWT token
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody AuthRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Logged in successfully", response));
    }

    /**
     * Current Logged-in User Profile
     * Requires Authorization: Bearer <token>
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileDto>> getCurrentUser(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return new ResponseEntity<>(ApiResponse.error("Unauthorized: No active authentication"), HttpStatus.UNAUTHORIZED);
        }
        UserProfileDto profile = authService.getCurrentUserProfile(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("User profile retrieved successfully", profile));
    }
}
