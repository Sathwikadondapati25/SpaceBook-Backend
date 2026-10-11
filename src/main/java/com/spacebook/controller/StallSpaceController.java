package com.spacebook.controller;

import com.spacebook.dto.ApiResponse;
import com.spacebook.dto.stall.StallSpaceRequest;
import com.spacebook.entity.StallSpace;
import com.spacebook.service.StallSpaceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Stall space controller for organizers
 */
@RestController
@RequestMapping("/api/events/{eventId}/stalls")
@Tag(name = "Stall Spaces", description = "Stall space management endpoints")
public class StallSpaceController {

    private final StallSpaceService stallSpaceService;

    public StallSpaceController(StallSpaceService stallSpaceService) {
        this.stallSpaceService = stallSpaceService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ORGANIZER')")
    @Operation(summary = "Create stall space")
    public ResponseEntity<ApiResponse<StallSpace>> createStallSpace(@PathVariable Long eventId,
                                                                      @Valid @RequestBody StallSpaceRequest request,
                                                                      Authentication authentication) {
        Long organizerId = (Long) authentication.getPrincipal();
        StallSpace stall = stallSpaceService.createStallSpace(organizerId, eventId, request);
        return ResponseEntity.ok(ApiResponse.success(stall));
    }

    @PutMapping("/{stallId}")
    @PreAuthorize("hasRole('ORGANIZER')")
    @Operation(summary = "Update stall space")
    public ResponseEntity<ApiResponse<StallSpace>> updateStallSpace(@PathVariable Long eventId,
                                                                      @PathVariable Long stallId,
                                                                      @Valid @RequestBody StallSpaceRequest request,
                                                                      Authentication authentication) {
        Long organizerId = (Long) authentication.getPrincipal();
        StallSpace stall = stallSpaceService.updateStallSpace(organizerId, stallId, request);
        return ResponseEntity.ok(ApiResponse.success(stall));
    }

    @GetMapping("/{stallId}")
    @Operation(summary = "Get stall space by ID")
    public ResponseEntity<ApiResponse<StallSpace>> getStallSpace(@PathVariable Long stallId) {
        StallSpace stall = stallSpaceService.getStallSpace(stallId);
        return ResponseEntity.ok(ApiResponse.success(stall));
    }

    @GetMapping
    @Operation(summary = "Get all stalls for an event")
    public ResponseEntity<ApiResponse<List<StallSpace>>> getEventStalls(@PathVariable Long eventId) {
        List<StallSpace> stalls = stallSpaceService.getEventStalls(eventId);
        return ResponseEntity.ok(ApiResponse.success(stalls));
    }

    @DeleteMapping("/{stallId}")
    @PreAuthorize("hasRole('ORGANIZER')")
    @Operation(summary = "Delete stall space")
    public ResponseEntity<ApiResponse<Void>> deleteStallSpace(@PathVariable Long eventId,
                                                                @PathVariable Long stallId,
                                                                Authentication authentication) {
        Long organizerId = (Long) authentication.getPrincipal();
        stallSpaceService.deleteStallSpace(organizerId, stallId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
