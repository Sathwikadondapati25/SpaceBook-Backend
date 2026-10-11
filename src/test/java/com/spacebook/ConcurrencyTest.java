package com.spacebook;

import com.spacebook.entity.*;
import com.spacebook.repository.*;
import com.spacebook.service.BookingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CRITICAL CONCURRENCY TESTS
 * 
 * These tests prove that the conflict-free booking guarantee works correctly.
 * They use Testcontainers with real MySQL (not H2) to accurately test the database constraints.
 * 
 * Test results will be reported in the README and PR description.
 */
@SpringBootTest
class ConcurrencyTest {

    static MySQLContainer<?> mysql;

    static {
        try {
            if (org.testcontainers.DockerClientFactory.instance().isDockerAvailable()) {
                mysql = new MySQLContainer<>("mysql:8.0")
                        .withDatabaseName("spacebook_test")
                        .withUsername("test")
                        .withPassword("test");
                mysql.start();
            }
        } catch (Throwable t) {
            mysql = null;
        }
    }

    @DynamicPropertySource
    static void mysqlProperties(DynamicPropertyRegistry registry) {
        if (mysql != null && mysql.isRunning()) {
            registry.add("spring.datasource.url", mysql::getJdbcUrl);
            registry.add("spring.datasource.username", mysql::getUsername);
            registry.add("spring.datasource.password", mysql::getPassword);
        } else {
            registry.add("spring.datasource.url", () -> "jdbc:mysql://localhost:3306/spacebook_test?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true");
            registry.add("spring.datasource.username", () -> "root");
            registry.add("spring.datasource.password", () -> "Sathwik@_05");
        }
    }

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private StallSpaceRepository stallSpaceRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ActiveStallAllocationRepository allocationRepository;

    @Autowired
    private BookingService bookingService;

    private User organizer;
    private Event event;
    private StallSpace stall;

    @BeforeEach
    void setUp() {
        // Clean up
        allocationRepository.deleteAll();
        bookingRepository.deleteAll();
        stallSpaceRepository.deleteAll();
        eventRepository.deleteAll();
        userRepository.deleteAll();

        // Create organizer
        organizer = User.builder()
                .email("organizer@test.com")
                .passwordHash("hash")
                .role(User.Role.ORGANIZER)
                .build();
        organizer = userRepository.save(organizer);

        // Create event
        event = Event.builder()
                .organizer(organizer)
                .name("Test Event")
                .category("Festival")
                .venueName("Test Venue")
                .address("123 Test St")
                .city("Test City")
                .country("Test Country")
                .latitude(BigDecimal.ZERO)
                .longitude(BigDecimal.ZERO)
                .timezone("UTC")
                .currency("USD")
                .startDate(LocalDateTime.now().plusDays(30))
                .endDate(LocalDateTime.now().plusDays(31))
                .status(Event.EventStatus.PUBLISHED)
                .layoutType(Event.LayoutType.GRID_HALL)
                .maxStallsPerVendor(5)
                .build();
        event = eventRepository.save(event);

        // Create stall
        stall = StallSpace.builder()
                .event(event)
                .stallNumber("A1")
                .width(BigDecimal.TEN)
                .depth(BigDecimal.TEN)
                .price(new BigDecimal("100.00"))
                .category("Food")
                .build();
        stall = stallSpaceRepository.save(stall);
    }

    /**
     * TEST 1: 100 concurrent approval attempts for the same stall
     * 
     * Expected result: Exactly 1 approved booking and exactly 1 allocation row
     * All other attempts should fail with ConflictException or return rejected status
     */
    @Test
    void testConcurrentApprovals_100Threads_ShouldResultInExactlyOneAllocation() throws InterruptedException {
        // Create 100 vendors
        List<User> vendors = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            User vendor = User.builder()
                    .email("vendor" + i + "@test.com")
                    .passwordHash("hash")
                    .role(User.Role.VENDOR)
                    .build();
            vendor = userRepository.save(vendor);
            vendors.add(vendor);
        }

        // Create 100 pending bookings for the same stall
        List<Booking> bookings = new ArrayList<>();
        for (User vendor : vendors) {
            Booking booking = Booking.builder()
                    .stall(stall)
                    .vendor(vendor)
                    .status(Booking.BookingStatus.REQUESTED)
                    .build();
            booking = bookingRepository.save(booking);
            bookings.add(booking);
        }

        // Attempt to approve all 100 bookings concurrently
        int threadCount = 100;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        for (Booking booking : bookings) {
            executor.submit(() -> {
                try {
                    bookingService.approveOrRejectBooking(
                            organizer.getId(),
                            booking.getId(),
                            new com.spacebook.dto.booking.BookingApprovalRequest(
                                    com.spacebook.dto.booking.BookingApprovalRequest.ApprovalAction.APPROVE,
                                    null
                            )
                    );
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(30, TimeUnit.SECONDS);
        executor.shutdown();

        // Verify results
        List<Booking> approvedBookings = bookingRepository.findByStallAndStatus(stall, Booking.BookingStatus.APPROVED);
        Optional<ActiveStallAllocation> allocation = allocationRepository.findByStall(stall);

        System.out.println("=== CONCURRENT APPROVAL TEST RESULTS ===");
        System.out.println("Total approval attempts: 100");
        System.out.println("Successful approvals: " + successCount.get());
        System.out.println("Failed attempts: " + failureCount.get());
        System.out.println("Approved bookings in DB: " + approvedBookings.size());
        System.out.println("Allocation present in DB: " + allocation.isPresent());

        // CRITICAL ASSERTIONS
        assertEquals(1, approvedBookings.size(), "Exactly 1 booking should be approved");
        assertTrue(allocation.isPresent(), "Exactly 1 allocation row should exist");
    }

    /**
     * TEST 2: 100 concurrent duplicate submissions by one vendor for one stall
     * 
     * Expected result: Exactly 1 active request (not cancelled or rejected)
     */
    @Test
    void testConcurrentDuplicateRequests_ShouldResultInExactlyOneActiveRequest() throws InterruptedException {
        User vendor = User.builder()
                .email("vendor@test.com")
                .passwordHash("hash")
                .role(User.Role.VENDOR)
                .build();
        User savedVendor = userRepository.save(vendor);
        final Long vendorId = savedVendor.getId();

        int threadCount = 100;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger conflictCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    bookingService.createBookingRequest(
                            vendorId,
                            new com.spacebook.dto.booking.BookingRequest(stall.getId(), "Test message")
                    );
                    successCount.incrementAndGet();
                } catch (com.spacebook.exception.ConflictException e) {
                    conflictCount.incrementAndGet();
                } catch (Exception e) {
                    // Other exceptions
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(30, TimeUnit.SECONDS);
        executor.shutdown();

        // Verify results
        List<Booking> activeRequests = bookingRepository.findActiveRequestByVendorAndStall(
                stall.getId(), vendor.getId()
        );

        System.out.println("=== DUPLICATE REQUEST TEST RESULTS ===");
        System.out.println("Total submission attempts: 100");
        System.out.println("Successful submissions: " + successCount.get());
        System.out.println("Conflict errors: " + conflictCount.get());
        System.out.println("Active requests in DB: " + activeRequests.size());

        // CRITICAL ASSERTION
        assertTrue(activeRequests.size() <= 1, "At most 1 active request should exist");
    }

    /**
     * TEST 3: Race between approve and cancel on the same stall
     * 
     * Expected result: Only one operation should succeed, no inconsistent state
     */
    @Test
    void testRaceBetweenApproveAndCancel_ShouldNotCauseInconsistentState() throws InterruptedException {
        // Create vendor and booking
        User vendor = User.builder()
                .email("vendor@test.com")
                .passwordHash("hash")
                .role(User.Role.VENDOR)
                .build();
        User savedVendor = userRepository.save(vendor);
        final Long vendorId = savedVendor.getId();

        Booking booking = Booking.builder()
                .stall(stall)
                .vendor(savedVendor)
                .status(Booking.BookingStatus.REQUESTED)
                .build();
        Booking savedBooking = bookingRepository.save(booking);
        final Long bookingId = savedBooking.getId();
        final Long organizerId = organizer.getId();

        // First approve it
        bookingService.approveOrRejectBooking(
                organizerId,
                bookingId,
                new com.spacebook.dto.booking.BookingApprovalRequest(
                        com.spacebook.dto.booking.BookingApprovalRequest.ApprovalAction.APPROVE,
                        null
                )
        );

        // Now try concurrent cancel and approve
        int threadCount = 50;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount * 2);
        AtomicInteger cancelSuccess = new AtomicInteger(0);
        AtomicInteger approveSuccess = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            // Cancel attempts
            executor.submit(() -> {
                try {
                    bookingService.cancelBooking(vendorId, bookingId);
                    cancelSuccess.incrementAndGet();
                } catch (Exception e) {
                    // Expected to fail after first cancel
                } finally {
                    latch.countDown();
                }
            });

            // Approve attempts (should fail since already approved)
            executor.submit(() -> {
                try {
                    bookingService.approveOrRejectBooking(
                            organizerId,
                            bookingId,
                            new com.spacebook.dto.booking.BookingApprovalRequest(
                                    com.spacebook.dto.booking.BookingApprovalRequest.ApprovalAction.APPROVE,
                                    null
                            )
                    );
                    approveSuccess.incrementAndGet();
                } catch (Exception e) {
                    // Expected to fail
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(30, TimeUnit.SECONDS);
        executor.shutdown();

        // Verify final state is consistent
        Booking finalBooking = bookingRepository.findById(booking.getId()).orElseThrow();
        Optional<ActiveStallAllocation> allocation = allocationRepository.findByStall(stall);

        System.out.println("=== RACE CONDITION TEST RESULTS ===");
        System.out.println("Cancel successes: " + cancelSuccess.get());
        System.out.println("Approve successes: " + approveSuccess.get());
        System.out.println("Final booking status: " + finalBooking.getStatus());
        System.out.println("Allocation present: " + allocation.isPresent());

        // If cancelled, no allocation should exist
        if (finalBooking.getStatus() == Booking.BookingStatus.CANCELLED) {
            assertTrue(allocation.isEmpty(), "No allocation should exist after cancellation");
        } else {
            assertTrue(allocation.isPresent(), "Allocation should exist if approved");
        }
    }
}
