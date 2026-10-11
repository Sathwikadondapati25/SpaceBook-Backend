package com.spacebook.security;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Simple in-memory rate limiter for login attempts
 */
@Component
public class RateLimiter {

    private final ConcurrentHashMap<String, LoginAttemptInfo> attempts = new ConcurrentHashMap<>();

    public boolean isLocked(String identifier) {
        LoginAttemptInfo info = attempts.get(identifier);
        if (info == null) {
            return false;
        }

        // Reset if lockout period has passed
        if (info.getLockoutUntil() != null && LocalDateTime.now().isAfter(info.getLockoutUntil())) {
            attempts.remove(identifier);
            return false;
        }

        return info.getLockoutUntil() != null;
    }

    public void recordFailedAttempt(String identifier, int maxAttempts, int lockoutMinutes) {
        attempts.compute(identifier, (key, info) -> {
            if (info == null) {
                info = new LoginAttemptInfo();
            }
            info.incrementAttempts();

            if (info.getAttempts() >= maxAttempts) {
                info.setLockoutUntil(LocalDateTime.now().plusMinutes(lockoutMinutes));
            }

            return info;
        });
    }

    public void resetAttempts(String identifier) {
        attempts.remove(identifier);
    }

    private static class LoginAttemptInfo {
        private final AtomicInteger attempts = new AtomicInteger(0);
        private LocalDateTime lockoutUntil;

        public int getAttempts() {
            return attempts.get();
        }

        public void incrementAttempts() {
            attempts.incrementAndGet();
        }

        public LocalDateTime getLockoutUntil() {
            return lockoutUntil;
        }

        public void setLockoutUntil(LocalDateTime lockoutUntil) {
            this.lockoutUntil = lockoutUntil;
        }
    }
}
