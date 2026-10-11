package com.spacebook.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * CRITICAL: This table enforces conflict-free booking at the database level
 * The PRIMARY KEY on stall_id makes it impossible to have two active allocations
 * for the same stall. This is the foundation of our double-booking prevention.
 */
@Entity
@Table(name = "active_stall_allocations")
public class ActiveStallAllocation {

    @Id
    @Column(name = "stall_id", nullable = false)
    private Long stallId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stall_id", nullable = false, insertable = false, updatable = false)
    private StallSpace stall;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @Column(name = "allocated_at", updatable = false)
    private LocalDateTime allocatedAt;

    public ActiveStallAllocation() {
    }

    public ActiveStallAllocation(StallSpace stall, Booking booking) {
        this.stall = stall;
        this.booking = booking;
        if (stall != null) {
            this.stallId = stall.getId();
        }
    }

    @PrePersist
    protected void onCreate() {
        allocatedAt = LocalDateTime.now();
        if (stall != null) {
            stallId = stall.getId();
        }
    }

    // Getters and Setters
    public Long getStallId() { return stallId; }
    public void setStallId(Long stallId) { this.stallId = stallId; }

    public StallSpace getStall() { return stall; }
    public void setStall(StallSpace stall) { this.stall = stall; }

    public Booking getBooking() { return booking; }
    public void setBooking(Booking booking) { this.booking = booking; }

    public LocalDateTime getAllocatedAt() { return allocatedAt; }
    public void setAllocatedAt(LocalDateTime allocatedAt) { this.allocatedAt = allocatedAt; }
}
