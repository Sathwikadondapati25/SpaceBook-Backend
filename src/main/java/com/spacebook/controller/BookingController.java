package com.spacebook.controller;

import com.spacebook.dto.ApiResponse;
import com.spacebook.dto.booking.BookingApprovalRequest;
import com.spacebook.dto.booking.BookingRequest;
import com.spacebook.entity.Booking;
import com.spacebook.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Booking controller for vendors and organizers
 */
@RestController
@RequestMapping("/api/bookings")
@Tag(name = "Bookings", description = "Booking management endpoints")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    @PreAuthorize("hasRole('VENDOR')")
    @Operation(summary = "Create booking request (vendor)")
    public ResponseEntity<ApiResponse<Booking>> createBooking(@Valid @RequestBody BookingRequest request,
                                                              Authentication authentication) {
        Long vendorId = (Long) authentication.getPrincipal();
        Booking booking = bookingService.createBookingRequest(vendorId, request);
        return ResponseEntity.ok(ApiResponse.success(booking));
    }

    @RequestMapping(value = {"/{bookingId}/approve", "/{bookingId}/approval"}, method = {RequestMethod.POST, RequestMethod.PUT})
    @PreAuthorize("hasRole('ORGANIZER')")
    @Operation(summary = "Approve or reject booking (organizer)")
    public ResponseEntity<ApiResponse<Booking>> approveOrRejectBooking(@PathVariable Long bookingId,
                                                                          @Valid @RequestBody BookingApprovalRequest request,
                                                                          Authentication authentication) {
        Long organizerId = (Long) authentication.getPrincipal();
        Booking booking = bookingService.approveOrRejectBooking(organizerId, bookingId, request);
        return ResponseEntity.ok(ApiResponse.success(booking));
    }

    @PostMapping("/{bookingId}/cancel")
    @PreAuthorize("hasRole('VENDOR')")
    @Operation(summary = "Cancel booking (vendor)")
    public ResponseEntity<ApiResponse<Booking>> cancelBooking(@PathVariable Long bookingId,
                                                               Authentication authentication) {
        Long vendorId = (Long) authentication.getPrincipal();
        Booking booking = bookingService.cancelBooking(vendorId, bookingId);
        return ResponseEntity.ok(ApiResponse.success(booking));
    }

    @GetMapping({"/my", "/my-bookings"})
    @PreAuthorize("hasRole('VENDOR')")
    @Operation(summary = "Get vendor's bookings")
    public ResponseEntity<ApiResponse<List<Booking>>> getMyBookings(Authentication authentication) {
        Long vendorId = (Long) authentication.getPrincipal();
        List<Booking> bookings = bookingService.getVendorBookings(vendorId);
        return ResponseEntity.ok(ApiResponse.success(bookings));
    }

    @GetMapping("/event/{eventId}")
    @PreAuthorize("hasRole('ORGANIZER')")
    @Operation(summary = "Get bookings for an event (organizer)")
    public ResponseEntity<ApiResponse<List<Booking>>> getEventBookings(@PathVariable Long eventId,
                                                                        Authentication authentication) {
        Long organizerId = (Long) authentication.getPrincipal();
        List<Booking> bookings = bookingService.getEventBookings(organizerId, eventId);
        return ResponseEntity.ok(ApiResponse.success(bookings));
    }

    @GetMapping("/{bookingId}")
    @Operation(summary = "Get booking by ID")
    public ResponseEntity<ApiResponse<Booking>> getBooking(@PathVariable Long bookingId,
                                                           Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        Booking booking = bookingService.getBooking(userId, bookingId);
        return ResponseEntity.ok(ApiResponse.success(booking));
    }
}
