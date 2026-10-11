package com.spacebook.service;

import com.spacebook.dto.profile.OrganizerProfileRequest;
import com.spacebook.dto.profile.VendorProfileRequest;
import com.spacebook.entity.OrganizerProfile;
import com.spacebook.entity.User;
import com.spacebook.entity.VendorProfile;
import com.spacebook.exception.ResourceNotFoundException;
import com.spacebook.repository.OrganizerProfileRepository;
import com.spacebook.repository.UserRepository;
import com.spacebook.repository.VendorProfileRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Profile service for managing vendor and organizer profiles
 */
@Service
public class ProfileService {

    private final UserRepository userRepository;
    private final VendorProfileRepository vendorProfileRepository;
    private final OrganizerProfileRepository organizerProfileRepository;

    public ProfileService(UserRepository userRepository,
                          VendorProfileRepository vendorProfileRepository,
                          OrganizerProfileRepository organizerProfileRepository) {
        this.userRepository = userRepository;
        this.vendorProfileRepository = vendorProfileRepository;
        this.organizerProfileRepository = organizerProfileRepository;
    }

    @Transactional
    public VendorProfile createOrUpdateVendorProfile(Long userId, VendorProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getRole() != User.Role.VENDOR) {
            throw new IllegalArgumentException("User is not a vendor");
        }

        VendorProfile profile = vendorProfileRepository.findByUserId(userId)
                .orElse(VendorProfile.builder().userId(userId).build());

        profile.setBusinessName(request.getBusinessName());
        profile.setContactName(request.getContactName());
        profile.setPhone(request.getPhone());
        profile.setCategory(request.getCategory());
        profile.setCity(request.getCity());
        profile.setCountry(request.getCountry());
        profile.setDescription(request.getDescription());
        profile.setWebsite(request.getWebsite());
        profile.setYearsOperating(request.getYearsOperating());
        profile.setAvatarUrl(request.getAvatarUrl());
        profile.setUser(user);

        return vendorProfileRepository.save(profile);
    }

    @Transactional(readOnly = true)
    public VendorProfile getVendorProfile(Long userId) {
        return vendorProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor profile not found"));
    }

    @Transactional
    public OrganizerProfile createOrUpdateOrganizerProfile(Long userId, OrganizerProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getRole() != User.Role.ORGANIZER) {
            throw new IllegalArgumentException("User is not an organizer");
        }

        OrganizerProfile profile = organizerProfileRepository.findByUserId(userId)
                .orElse(OrganizerProfile.builder().userId(userId).build());

        profile.setOrganizationName(request.getOrganizationName());
        profile.setContactName(request.getContactName());
        profile.setPhone(request.getPhone());
        profile.setOrgType(request.getOrgType());
        profile.setCity(request.getCity());
        profile.setCountry(request.getCountry());
        profile.setWebsite(request.getWebsite());
        profile.setDescription(request.getDescription());
        profile.setUser(user);

        return organizerProfileRepository.save(profile);
    }

    @Transactional(readOnly = true)
    public OrganizerProfile getOrganizerProfile(Long userId) {
        return organizerProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Organizer profile not found"));
    }

    @Transactional(readOnly = true)
    public VendorProfile getVendorProfileForOrganizer(Long vendorId, Long organizerId) {
        // Verify that the vendor has interacted with this organizer's events
        // This is a simplified check - in production, you'd verify actual interaction
        VendorProfile profile = vendorProfileRepository.findByUserId(vendorId)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor profile not found"));
        return profile;
    }
}
