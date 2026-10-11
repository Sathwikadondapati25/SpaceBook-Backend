package com.spacebook.service;

import com.spacebook.dto.auth.AuthResponse;
import com.spacebook.dto.auth.LoginRequest;
import com.spacebook.dto.auth.RegisterRequest;
import com.spacebook.entity.RefreshToken;
import com.spacebook.entity.User;
import com.spacebook.exception.BadRequestException;
import com.spacebook.exception.ConflictException;
import com.spacebook.exception.ResourceNotFoundException;
import com.spacebook.repository.RefreshTokenRepository;
import com.spacebook.repository.UserRepository;
import com.spacebook.security.JwtTokenProvider;
import com.spacebook.security.RateLimiter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Authentication service for login, register, token refresh, and logout
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final RateLimiter rateLimiter;

    public AuthService(UserRepository userRepository,
                       RefreshTokenRepository refreshTokenRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider,
                       AuthenticationManager authenticationManager,
                       RateLimiter rateLimiter) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.authenticationManager = authenticationManager;
        this.rateLimiter = rateLimiter;
    }

    @Value("${spacebook.security.organizer-invite-code}")
    private String organizerInviteCode;

    @Value("${spacebook.security.jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    @Value("${spacebook.security.rate-limit.login-attempts}")
    private int maxLoginAttempts;

    @Value("${spacebook.security.rate-limit.lockout-minutes}")
    private int lockoutMinutes;

    @Transactional
    public AuthResponse login(LoginRequest request) {
        String email = request.getEmail();

        // Check rate limiting
        if (rateLimiter.isLocked(email)) {
            throw new BadRequestException("Account temporarily locked due to too many failed attempts");
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, request.getPassword())
            );

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            // Reset failed attempts on successful login
            rateLimiter.resetAttempts(email);

            // Generate tokens
            String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getEmail(), user.getRole().name());
            String refreshTokenValue = jwtTokenProvider.generateRefreshToken(user.getId());

            // Save refresh token
            RefreshToken refreshToken = RefreshToken.builder()
                    .user(user)
                    .token(refreshTokenValue)
                    .expiresAt(LocalDateTime.now().plus(refreshTokenExpiration, java.time.temporal.ChronoUnit.MILLIS))
                    .build();
            refreshTokenRepository.save(refreshToken);

            return AuthResponse.builder()
                    .accessToken(accessToken)
                    .userId(user.getId())
                    .email(user.getEmail())
                    .role(user.getRole().name())
                    .build();

        } catch (Exception e) {
            rateLimiter.recordFailedAttempt(email, maxLoginAttempts, lockoutMinutes);
            throw e;
        }
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email already registered");
        }

        // Validate organizer invite code
        if ("ORGANIZER".equalsIgnoreCase(request.getRole())) {
            if (request.getOrganizerInviteCode() == null || 
                !request.getOrganizerInviteCode().equals(organizerInviteCode)) {
                throw new BadRequestException("Invalid organizer invite code");
            }
        }

        // Create user
        User.Role role = User.Role.valueOf(request.getRole().toUpperCase());
        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .build();
        user = userRepository.save(user);

        // Generate access token
        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getEmail(), user.getRole().name());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }

    @Transactional
    public AuthResponse refreshToken(String refreshTokenValue) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenValue)
                .orElseThrow(() -> new BadRequestException("Invalid refresh token"));

        if (refreshToken.isRevoked() || refreshToken.isExpired()) {
            throw new BadRequestException("Refresh token expired or revoked");
        }

        User user = refreshToken.getUser();
        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getEmail(), user.getRole().name());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }

    @Transactional
    public void logout(String refreshTokenValue) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenValue)
                .orElseThrow(() -> new BadRequestException("Invalid refresh token"));

        refreshToken.setRevokedAt(LocalDateTime.now());
        refreshTokenRepository.save(refreshToken);
    }

    @Transactional
    public void logoutAll(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        refreshTokenRepository.deleteByUser(user);
    }
}
