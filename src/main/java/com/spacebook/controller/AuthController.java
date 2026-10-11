package com.spacebook.controller;

import com.spacebook.dto.ApiResponse;
import com.spacebook.dto.auth.AuthResponse;
import com.spacebook.dto.auth.LoginRequest;
import com.spacebook.dto.auth.RegisterRequest;
import com.spacebook.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication controller
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Authentication endpoints")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @Operation(summary = "Login user")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request,
                                                           HttpServletResponse response) {
        AuthResponse authResponse = authService.login(request);

        // Set refresh token as httpOnly cookie
        // Note: In a real implementation, you'd generate the refresh token here
        // For now, we're returning only the access token

        return ResponseEntity.ok(ApiResponse.success(authResponse));
    }

    @PostMapping("/register")
    @Operation(summary = "Register new user")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse authResponse = authService.register(request);
        return ResponseEntity.ok(ApiResponse.success(authResponse));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(@CookieValue(name = "refreshToken", required = false) String refreshToken) {
        if (refreshToken == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Refresh token not provided"));
        }
        AuthResponse authResponse = authService.refreshToken(refreshToken);
        return ResponseEntity.ok(ApiResponse.success(authResponse));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout user")
    public ResponseEntity<ApiResponse<Void>> logout(@CookieValue(name = "refreshToken", required = false) String refreshToken,
                                                   HttpServletResponse response) {
        if (refreshToken != null) {
            authService.logout(refreshToken);
        }

        // Clear refresh token cookie
        Cookie cookie = new Cookie("refreshToken", "");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);

        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
