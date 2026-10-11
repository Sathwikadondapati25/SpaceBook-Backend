package com.spacebook.service;

import com.spacebook.dto.booking.BookingApprovalRequest;
import com.spacebook.dto.booking.BookingRequest;
import com.spacebook.entity.*;
import com.spacebook.entity.Booking.BookingStatus;
import com.spacebook.entity.Event.EventStatus;
import com.spacebook.exception.BadRequestException;
import com.spacebook.exception.ConflictException;
import com.spacebook.exception.ResourceNotFoundException;
import com.spacebook.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Booking service with conflict-free approval transaction
 * 
 * CRITICAL: This service implements the core guarantee that a stall can never be
 * confirmed for two vendors. The approval method uses:
 * 1. Pessimistic locking on the stall row
 * 2. Re-checking availability within the transaction
 * 3. Inserting into active_stall_allocations with UNIQUE(stall_id) constraint
 * 4. Rejecting all competing pending requests atomically
 * 
 * If any concurrent transaction tries to approve the same stall, the UNIQUE constraint
 * will fail and the transaction will roll back, ensuring exactly one approval succeeds.
 */
@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final StallSpaceRepository stallSpaceRepository;
    private final ActiveStallAllocationRepository allocationRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;
    private final NotificationService notificationService;

    public BookingService(BookingRepository bookingRepository,
                          StallSpaceRepository stallSpaceRepository,
                          ActiveStallAllocationRepository allocationRepository,
                          EventRepository eventRepository,
                          UserRepository userRepository,
                          AuditLogService auditLogService,
                          NotificationService notificationService) {
        this.bookingRepository = bookingRepository;
        this.stallSpaceRepository = stallSpaceRepository;
        this.allocationRepository = allocationRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.auditLogService = auditLogService;
        this.notificationService = notificationService;
    }

    @Transactional
    public Booking createBookingRequest(Long vendorId, BookingRequest request) {
        User vendor = userRepository.findById(vendorId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (vendor.getRole() != User.Role.VENDOR) {
            throw new BadRequestException("Only vendors can request bookings");
        }

        StallSpace stall = stallSpaceRepository.findByIdWithLock(request.getStallId())
                .orElseThrow(() -> new ResourceNotFoundException("Stall not found"));

        Event event = stall.getEvent();

        // Check if stall is already allocated
        allocationRepository.findByStall(stall).ifPresent(allocation -> {
            throw new ConflictException("Stall is already allocated");
        });

        // Validate event status
        if (event.getStatus() != EventStatus.PUBLISHED) {
            throw new BadRequestException("Event is not open for bookings");
        }

        // Validate booking window
        if (event.getBookingWindowStart() != null && LocalDateTime.now().isBefore(event.getBookingWindowStart())) {
            throw new BadRequestException("Booking window has not opened yet");
        }
        if (event.getBookingWindowEnd() != null && LocalDateTime.now().isAfter(event.getBookingWindowEnd())) {
            throw new BadRequestException("Booking window has closed");
        }

        // Check for duplicate active request
        if (!bookingRepository.findActiveRequestByVendorAndStallWithLock(stall.getId(), vendorId).isEmpty()) {
            throw new ConflictException("You already have an active request for this stall");
        }

        // Check per-vendor limit
        long currentBookings = bookingRepository.findByVendor(vendor).stream()
                .filter(b -> b.getStatus() == BookingStatus.APPROVED || b.getStatus() == BookingStatus.REQUESTED)
                .count();
        if (currentBookings >= event.getMaxStallsPerVendor()) {
            throw new BadRequestException("You have reached the maximum number of stalls per vendor for this event");
        }

        // Create booking request
        Booking booking = Booking.builder()
                .stall(stall)
                .vendor(vendor)
                .status(BookingStatus.REQUESTED)
                .message(request.getMessage())
                .build();

        booking = bookingRepository.save(booking);

        // Notify organizer
        notificationService.createNotification(
                event.getOrganizer().getId(),
                "BOOKING_REQUEST",
                "New Booking Request",
                String.format("Vendor requested stall %s for event %s", stall.getStallNumber(), event.getName()),
                "BOOKING",
                booking.getId()
        );

        // Audit log
        auditLogService.createAuditLog(
                vendorId,
                "BOOKING_REQUEST",
                "BOOKING",
                booking.getId(),
                String.format("Vendor requested stall %s", stall.getStallNumber())
        );

        return booking;
    }

    /**
     * CRITICAL: This method implements the conflict-free approval guarantee.
     * The entire operation is in a single transaction with pessimistic locking.
     */
    @Transactional
    public Booking approveOrRejectBooking(Long organizerId, Long bookingId, BookingApprovalRequest request) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        // Verify ownership
        if (!booking.getStall().getEvent().getOrganizer().getId().equals(organizerId)) {
            throw new BadRequestException("You can only manage bookings for your own events");
        }

        if (booking.getStatus() != BookingStatus.REQUESTED) {
            throw new BadRequestException("Booking has already been processed");
        }

        if (request.getAction() == BookingApprovalRequest.ApprovalAction.APPROVE) {
            return approveBooking(booking, organizerId);
        } else {
            return rejectBooking(booking, organizerId, request.getRejectionReason());
        }
    }

    private Booking approveBooking(Booking booking, Long organizerId) {
        StallSpace stall = booking.getStall();

        // CRITICAL: Lock the stall row pessimistically to prevent concurrent modifications
        StallSpace lockedStall = stallSpaceRepository.findByIdWithLock(stall.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Stall not found"));

        // Re-check availability within the transaction
        allocationRepository.findByStall(lockedStall).ifPresent(allocation -> {
            throw new ConflictException("Stall is no longer available");
        });

        // CRITICAL: Insert allocation - this will fail if another transaction already inserted
        // due to the UNIQUE(stall_id) constraint
        ActiveStallAllocation allocation = new ActiveStallAllocation(lockedStall, booking);
        allocationRepository.save(allocation);

        // Update booking status
        booking.setStatus(BookingStatus.APPROVED);
        booking = bookingRepository.save(booking);

        // Reject all other pending requests for this stall
        List<Booking> pendingRequests = bookingRepository.findPendingRequestsByStall(lockedStall.getId());
        for (Booking pending : pendingRequests) {
            if (!pending.getId().equals(booking.getId())) {
                pending.setStatus(BookingStatus.REJECTED);
                pending.setRejectionReason("Stall was allocated to another vendor");
                bookingRepository.save(pending);

                // Notify rejected vendor
                notificationService.createNotification(
                        pending.getVendor().getId(),
                        "BOOKING_REJECTED",
                        "Booking Rejected",
                        String.format("Your request for stall %s was rejected", lockedStall.getStallNumber()),
                        "BOOKING",
                        pending.getId()
                );
            }
        }

        // Notify approved vendor
        notificationService.createNotification(
                booking.getVendor().getId(),
                "BOOKING_APPROVED",
                "Booking Approved",
                String.format("Your request for stall %s has been approved", lockedStall.getStallNumber()),
                "BOOKING",
                booking.getId()
        );

        // Audit log
        auditLogService.createAuditLog(
                organizerId,
                "BOOKING_APPROVE",
                "BOOKING",
                booking.getId(),
                String.format("Approved booking for stall %s", lockedStall.getStallNumber())
        );

        return booking;
    }

    private Booking rejectBooking(Booking booking, Long organizerId, String reason) {
        booking.setStatus(BookingStatus.REJECTED);
        booking.setRejectionReason(reason);
        booking = bookingRepository.save(booking);

        // Notify vendor
        notificationService.createNotification(
                booking.getVendor().getId(),
                "BOOKING_REJECTED",
                "Booking Rejected",
                String.format("Your request for stall %s was rejected: %s", 
                    booking.getStall().getStallNumber(), reason),
                "BOOKING",
                booking.getId()
        );

        // Audit log
        auditLogService.createAuditLog(
                organizerId,
                "BOOKING_REJECT",
                "BOOKING",
                booking.getId(),
                String.format("Rejected booking for stall %s", booking.getStall().getStallNumber())
        );

        return booking;
    }

    @Transactional
    public Booking cancelBooking(Long vendorId, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        if (!booking.getVendor().getId().equals(vendorId)) {
            throw new BadRequestException("You can only cancel your own bookings");
        }

        if (booking.getStatus() != BookingStatus.APPROVED) {
            throw new BadRequestException("Only approved bookings can be cancelled");
        }

        // CRITICAL: Delete allocation to free the stall
        allocationRepository.deleteByStall(booking.getStall());

        booking.setStatus(BookingStatus.CANCELLED);
        booking = bookingRepository.save(booking);

        // Notify organizer
        notificationService.createNotification(
                booking.getStall().getEvent().getOrganizer().getId(),
                "BOOKING_CANCELLED",
                "Booking Cancelled",
                String.format("Vendor cancelled booking for stall %s", booking.getStall().getStallNumber()),
                "BOOKING",
                booking.getId()
        );

        // Audit log
        auditLogService.createAuditLog(
                vendorId,
                "BOOKING_CANCEL",
                "BOOKING",
                booking.getId(),
                String.format("Cancelled booking for stall %s", booking.getStall().getStallNumber())
        );

        return booking;
    }

    @Transactional(readOnly = true)
    public List<Booking> getVendorBookings(Long vendorId) {
        User vendor = userRepository.findById(vendorId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return bookingRepository.findByVendor(vendor);
    }

    @Transactional(readOnly = true)
    public List<Booking> getEventBookings(Long organizerId, Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        if (!event.getOrganizer().getId().equals(organizerId)) {
            throw new BadRequestException("You can only view bookings for your own events");
        }

        // Get all stalls for the event, then get bookings for those stalls
        List<StallSpace> stalls = stallSpaceRepository.findByEvent(event);
        return stalls.stream()
                .flatMap(stall -> bookingRepository.findByStall(stall).stream())
                .toList();
    }

    @Transactional(readOnly = true)
    public Booking getBooking(Long userId, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
        boolean isVendor = booking.getVendor().getId().equals(userId);
        boolean isOrganizer = booking.getStall().getEvent().getOrganizer().getId().equals(userId);
        if (!isVendor && !isOrganizer) {
            throw new BadRequestException("You do not have permission to view this booking");
        }
        return booking;
    }
}
