package com.spacebook.dto.auth;

public class AuthResponse {

    private String accessToken;
    private Long userId;
    private String email;
    private String role;

    public AuthResponse() {
    }

    public AuthResponse(String accessToken, Long userId, String email, String role) {
        this.accessToken = accessToken;
        this.userId = userId;
        this.email = email;
        this.role = role;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String accessToken;
        private Long userId;
        private String email;
        private String role;

        public Builder accessToken(String accessToken) { this.accessToken = accessToken; return this; }
        public Builder userId(Long userId) { this.userId = userId; return this; }
        public Builder email(String email) { this.email = email; return this; }
        public Builder role(String role) { this.role = role; return this; }

        public AuthResponse build() {
            return new AuthResponse(accessToken, userId, email, role);
        }
    }
}
