package com.spacebook;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spacebook.dto.auth.RegisterRequest;
import com.spacebook.entity.User;
import com.spacebook.repository.UserRepository;
import com.spacebook.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class SecurityAuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private String vendorToken;
    private String organizerToken;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        // Create vendor
        User vendor = User.builder()
                .email("vendor.auth@example.com")
                .passwordHash("$2a$10$abcdefghijklmnopqrstuv")
                .role(User.Role.VENDOR)
                .build();
        vendor = userRepository.save(vendor);
        vendorToken = "Bearer " + jwtTokenProvider.generateAccessToken(vendor.getId(), vendor.getEmail(), "VENDOR");

        // Create organizer
        User organizer = User.builder()
                .email("organizer.auth@example.com")
                .passwordHash("$2a$10$abcdefghijklmnopqrstuv")
                .role(User.Role.ORGANIZER)
                .build();
        organizer = userRepository.save(organizer);
        organizerToken = "Bearer " + jwtTokenProvider.generateAccessToken(organizer.getId(), organizer.getEmail(), "ORGANIZER");
    }

    @Test
    @DisplayName("Unauthenticated request to protected endpoint returns 401 Unauthorized")
    void testUnauthenticated_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/bookings/my"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Malformed JWT token returns 401 Unauthorized")
    void testMalformedJwt_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/bookings/my")
                        .header("Authorization", "Bearer invalid.jwt.token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Vendor calling Organizer-only endpoint (get my events) returns 403 Forbidden")
    void testVendorAccessingOrganizerEndpoint_Forbidden() throws Exception {
        mockMvc.perform(get("/api/events/my-events")
                        .header("Authorization", vendorToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Vendor calling Organizer-only endpoint (approve booking) returns 403 Forbidden")
    void testVendorApprovingBooking_Forbidden() throws Exception {
        mockMvc.perform(put("/api/bookings/1/approval")
                        .header("Authorization", vendorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"action\":\"APPROVE\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Organizer calling Vendor-only endpoint (create booking) returns 403 Forbidden")
    void testOrganizerAccessingVendorEndpoint_Forbidden() throws Exception {
        mockMvc.perform(post("/api/bookings")
                        .header("Authorization", organizerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"stallId\":1}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Organizer calling Vendor-only endpoint (my bookings) returns 403 Forbidden")
    void testOrganizerAccessingMyBookings_Forbidden() throws Exception {
        mockMvc.perform(get("/api/bookings/my")
                        .header("Authorization", organizerToken))
                .andExpect(status().isForbidden());
    }
}
