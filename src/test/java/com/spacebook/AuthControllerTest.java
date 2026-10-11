package com.spacebook;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spacebook.dto.auth.LoginRequest;
import com.spacebook.dto.auth.RegisterRequest;
import com.spacebook.entity.User;
import com.spacebook.repository.RefreshTokenRepository;
import com.spacebook.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @BeforeEach
    void setUp() {
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Should register a vendor successfully and exclude passwordHash")
    void testRegisterVendor_Success() throws Exception {
        RegisterRequest request = new RegisterRequest("vendor1@example.com", "SecurePassword123!", "VENDOR", null);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("vendor1@example.com"))
                .andExpect(jsonPath("$.data.role").value("VENDOR"))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.passwordHash").doesNotExist())
                .andExpect(jsonPath("$.data.password").doesNotExist());
    }

    @Test
    @DisplayName("Should reject registration if password is under 10 characters")
    void testRegisterVendor_ShortPassword_BadRequest() throws Exception {
        RegisterRequest request = new RegisterRequest("short@example.com", "short99", "VENDOR", null);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should reject organizer registration with missing or invalid invite code")
    void testRegisterOrganizer_InvalidInviteCode() throws Exception {
        RegisterRequest request = new RegisterRequest("org@example.com", "SecurePassword123!", "ORGANIZER", "WRONG-CODE");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("Should register organizer with valid invite code")
    void testRegisterOrganizer_Success() throws Exception {
        RegisterRequest request = new RegisterRequest("org@example.com", "SecurePassword123!", "ORGANIZER", "ORG-2024-SECRET");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("org@example.com"))
                .andExpect(jsonPath("$.data.role").value("ORGANIZER"));
    }

    @Test
    @DisplayName("Should reject registration with duplicate email")
    void testRegisterDuplicateEmail_Conflict() throws Exception {
        RegisterRequest request = new RegisterRequest("dup@example.com", "SecurePassword123!", "VENDOR", null);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("Should login successfully with valid credentials")
    void testLogin_Success() throws Exception {
        RegisterRequest reg = new RegisterRequest("login@example.com", "Password12345!", "VENDOR", null);
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reg)))
                .andExpect(status().isOk());

        LoginRequest login = new LoginRequest("login@example.com", "Password12345!");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("login@example.com"))
                .andExpect(jsonPath("$.data.role").value("VENDOR"))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty());
    }

    @Test
    @DisplayName("Should reject login with invalid password")
    void testLogin_InvalidPassword_Fails() throws Exception {
        RegisterRequest reg = new RegisterRequest("loginbad@example.com", "Password12345!", "VENDOR", null);
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reg)))
                .andExpect(status().isOk());

        LoginRequest login = new LoginRequest("loginbad@example.com", "WrongPassword!");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isUnauthorized());
    }
}
