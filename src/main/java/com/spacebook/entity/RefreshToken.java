package com.spacebook.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Refresh token for JWT rotation
 * Stored in httpOnly, Secure, SameSite cookie
 */
@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @JsonIgnore
    @Column(nullable = false, unique = true, length = 500)
    private String token;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public RefreshToken() {
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public boolean isRevoked() {
        return revokedAt != null;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    public LocalDateTime getRevokedAt() { return revokedAt; }
    public void setRevokedAt(LocalDateTime revokedAt) { this.revokedAt = revokedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private User user;
        private String token;
        private LocalDateTime expiresAt;
        private LocalDateTime revokedAt;

        public Builder user(User user) { this.user = user; return this; }
        public Builder token(String token) { this.token = token; return this; }
        public Builder expiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; return this; }
        public Builder revokedAt(LocalDateTime revokedAt) { this.revokedAt = revokedAt; return this; }

        public RefreshToken build() {
            RefreshToken r = new RefreshToken();
            r.setUser(this.user);
            r.setToken(this.token);
            r.setExpiresAt(this.expiresAt);
            r.setRevokedAt(this.revokedAt);
            return r;
        }
    }
}
