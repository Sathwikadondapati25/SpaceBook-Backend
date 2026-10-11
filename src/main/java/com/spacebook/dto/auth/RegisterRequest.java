package com.spacebook.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegisterRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 10, message = "Password must be at least 10 characters")
    private String password;

    @NotBlank(message = "Role is required")
    private String role; // VENDOR or ORGANIZER

    private String organizerInviteCode; // Required for ORGANIZER role

    public RegisterRequest() {
    }

    public RegisterRequest(String email, String password, String role, String organizerInviteCode) {
        this.email = email;
        this.password = password;
        this.role = role;
        this.organizerInviteCode = organizerInviteCode;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getOrganizerInviteCode() { return organizerInviteCode; }
    public void setOrganizerInviteCode(String organizerInviteCode) { this.organizerInviteCode = organizerInviteCode; }
}
