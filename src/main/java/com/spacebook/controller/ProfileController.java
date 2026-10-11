package com.spacebook.controller;

import com.spacebook.dto.ApiResponse;
import com.spacebook.dto.profile.OrganizerProfileRequest;
import com.spacebook.dto.profile.VendorProfileRequest;
import com.spacebook.entity.OrganizerProfile;
import com.spacebook.entity.VendorProfile;
import com.spacebook.service.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Profile controller for managing user profiles
 */
@RestController
@RequestMapping("/api/profile")
@Tag(name = "Profile", description = "Profile management endpoints")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping("/vendor")
    @PreAuthorize("hasRole('VENDOR')")
    @Operation(summary = "Get vendor profile")
    public ResponseEntity<ApiResponse<VendorProfile>> getVendorProfile(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        VendorProfile profile = profileService.getVendorProfile(userId);
        return ResponseEntity.ok(ApiResponse.success(profile));
    }

    @PostMapping("/vendor")
    @PreAuthorize("hasRole('VENDOR')")
    @Operation(summary = "Create or update vendor profile")
    public ResponseEntity<ApiResponse<VendorProfile>> createOrUpdateVendorProfile(
            @Valid @RequestBody VendorProfileRequest request,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        VendorProfile profile = profileService.createOrUpdateVendorProfile(userId, request);
        return ResponseEntity.ok(ApiResponse.success(profile));
    }

    @GetMapping("/organizer")
    @PreAuthorize("hasRole('ORGANIZER')")
    @Operation(summary = "Get organizer profile")
    public ResponseEntity<ApiResponse<OrganizerProfile>> getOrganizerProfile(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        OrganizerProfile profile = profileService.getOrganizerProfile(userId);
        return ResponseEntity.ok(ApiResponse.success(profile));
    }

    @PostMapping("/organizer")
    @PreAuthorize("hasRole('ORGANIZER')")
    @Operation(summary = "Create or update organizer profile")
    public ResponseEntity<ApiResponse<OrganizerProfile>> createOrUpdateOrganizerProfile(
            @Valid @RequestBody OrganizerProfileRequest request,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        OrganizerProfile profile = profileService.createOrUpdateOrganizerProfile(userId, request);
        return ResponseEntity.ok(ApiResponse.success(profile));
    }

    @GetMapping("/vendor/{vendorId}")
    @PreAuthorize("hasRole('ORGANIZER')")
    @Operation(summary = "Get vendor profile (for organizers)")
    public ResponseEntity<ApiResponse<VendorProfile>> getVendorProfileForOrganizer(
            @PathVariable Long vendorId,
            Authentication authentication) {
        Long organizerId = (Long) authentication.getPrincipal();
        VendorProfile profile = profileService.getVendorProfileForOrganizer(vendorId, organizerId);
        return ResponseEntity.ok(ApiResponse.success(profile));
    }
}
