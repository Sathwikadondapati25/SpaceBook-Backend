package com.spacebook.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Booking request with status tracking
 * Uses optimistic locking via @Version
 */
@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stall_id", nullable = false)
    private StallSpace stall;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id", nullable = false)
    private User vendor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status = BookingStatus.REQUESTED;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Version
    private Integer version;

    public Booking() {
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public StallSpace getStall() { return stall; }
    public void setStall(StallSpace stall) { this.stall = stall; }

    public User getVendor() { return vendor; }
    public void setVendor(User vendor) { this.vendor = vendor; }

    public BookingStatus getStatus() { return status; }
    public void setStatus(BookingStatus status) { this.status = status; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }

    public enum BookingStatus {
        REQUESTED,
        APPROVED,
        REJECTED,
        CANCELLED
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private StallSpace stall;
        private User vendor;
        private BookingStatus status = BookingStatus.REQUESTED;
        private String message;
        private String rejectionReason;

        public Builder stall(StallSpace stall) { this.stall = stall; return this; }
        public Builder vendor(User vendor) { this.vendor = vendor; return this; }
        public Builder status(BookingStatus status) { this.status = status; return this; }
        public Builder message(String message) { this.message = message; return this; }
        public Builder rejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; return this; }

        public Booking build() {
            Booking booking = new Booking();
            booking.setStall(this.stall);
            booking.setVendor(this.vendor);
            booking.setStatus(this.status);
            booking.setMessage(this.message);
            booking.setRejectionReason(this.rejectionReason);
            return booking;
        }
    }
}
