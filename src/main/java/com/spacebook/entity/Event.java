package com.spacebook.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Event entity with booking rules and organization settings
 */
@Entity
@Table(name = "events")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "organizer_id", nullable = false)
    private User organizer;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 100)
    private String category;

    @Column(name = "venue_name", nullable = false, length = 255)
    private String venueName;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String address;

    @Column(nullable = false, length = 100)
    private String city;

    @Column(nullable = false, length = 100)
    private String country;

    @Column(nullable = false, precision = 10, scale = 8)
    private BigDecimal latitude;

    @Column(nullable = false, precision = 11, scale = 8)
    private BigDecimal longitude;

    @Column(nullable = false, length = 50)
    private String timezone;

    @Column(nullable = false, length = 3, columnDefinition = "CHAR(3)")
    private String currency;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "layout_type", nullable = false)
    private LayoutType layoutType;

    @Column(name = "booking_window_start")
    private LocalDateTime bookingWindowStart;

    @Column(name = "booking_window_end")
    private LocalDateTime bookingWindowEnd;

    @Column(name = "max_stalls_per_vendor")
    private Integer maxStallsPerVendor = 5;

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_mode")
    private ApprovalMode approvalMode = ApprovalMode.MANUAL;

    @Column(name = "allowed_categories", columnDefinition = "JSON")
    private String allowedCategories;

    @Column(columnDefinition = "TEXT")
    private String rules;

    @Column(name = "cover_colour", length = 7)
    private String coverColour;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Event() {
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

    public User getOrganizer() { return organizer; }
    public void setOrganizer(User organizer) { this.organizer = organizer; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getVenueName() { return venueName; }
    public void setVenueName(String venueName) { this.venueName = venueName; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public BigDecimal getLatitude() { return latitude; }
    public void setLatitude(BigDecimal latitude) { this.latitude = latitude; }

    public BigDecimal getLongitude() { return longitude; }
    public void setLongitude(BigDecimal longitude) { this.longitude = longitude; }

    public String getTimezone() { return timezone; }
    public void setTimezone(String timezone) { this.timezone = timezone; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public LocalDateTime getStartDate() { return startDate; }
    public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }

    public LocalDateTime getEndDate() { return endDate; }
    public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }

    public EventStatus getStatus() { return status; }
    public void setStatus(EventStatus status) { this.status = status; }

    public LayoutType getLayoutType() { return layoutType; }
    public void setLayoutType(LayoutType layoutType) { this.layoutType = layoutType; }

    public LocalDateTime getBookingWindowStart() { return bookingWindowStart; }
    public void setBookingWindowStart(LocalDateTime bookingWindowStart) { this.bookingWindowStart = bookingWindowStart; }

    public LocalDateTime getBookingWindowEnd() { return bookingWindowEnd; }
    public void setBookingWindowEnd(LocalDateTime bookingWindowEnd) { this.bookingWindowEnd = bookingWindowEnd; }

    public Integer getMaxStallsPerVendor() { return maxStallsPerVendor; }
    public void setMaxStallsPerVendor(Integer maxStallsPerVendor) { this.maxStallsPerVendor = maxStallsPerVendor; }

    public ApprovalMode getApprovalMode() { return approvalMode; }
    public void setApprovalMode(ApprovalMode approvalMode) { this.approvalMode = approvalMode; }

    public String getAllowedCategories() { return allowedCategories; }
    public void setAllowedCategories(String allowedCategories) { this.allowedCategories = allowedCategories; }

    public String getRules() { return rules; }
    public void setRules(String rules) { this.rules = rules; }

    public String getCoverColour() { return coverColour; }
    public void setCoverColour(String coverColour) { this.coverColour = coverColour; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public enum EventStatus {
        DRAFT,
        PUBLISHED,
        CLOSED,
        CANCELLED,
        COMPLETED
    }

    public enum LayoutType {
        GRID_HALL,
        OPEN_GROUND,
        STREET_MARKET,
        MULTI_ZONE
    }

    public enum ApprovalMode {
        MANUAL,
        AUTO
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private User organizer;
        private String name;
        private String description;
        private String category;
        private String venueName;
        private String address;
        private String city;
        private String country;
        private BigDecimal latitude;
        private BigDecimal longitude;
        private String timezone;
        private String currency;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private EventStatus status = EventStatus.DRAFT;
        private LayoutType layoutType;
        private LocalDateTime bookingWindowStart;
        private LocalDateTime bookingWindowEnd;
        private Integer maxStallsPerVendor = 5;
        private ApprovalMode approvalMode = ApprovalMode.MANUAL;
        private String allowedCategories;
        private String rules;
        private String coverColour;

        public Builder organizer(User organizer) { this.organizer = organizer; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder category(String category) { this.category = category; return this; }
        public Builder venueName(String venueName) { this.venueName = venueName; return this; }
        public Builder address(String address) { this.address = address; return this; }
        public Builder city(String city) { this.city = city; return this; }
        public Builder country(String country) { this.country = country; return this; }
        public Builder latitude(BigDecimal latitude) { this.latitude = latitude; return this; }
        public Builder longitude(BigDecimal longitude) { this.longitude = longitude; return this; }
        public Builder timezone(String timezone) { this.timezone = timezone; return this; }
        public Builder currency(String currency) { this.currency = currency; return this; }
        public Builder startDate(LocalDateTime startDate) { this.startDate = startDate; return this; }
        public Builder endDate(LocalDateTime endDate) { this.endDate = endDate; return this; }
        public Builder status(EventStatus status) { this.status = status; return this; }
        public Builder layoutType(LayoutType layoutType) { this.layoutType = layoutType; return this; }
        public Builder bookingWindowStart(LocalDateTime bookingWindowStart) { this.bookingWindowStart = bookingWindowStart; return this; }
        public Builder bookingWindowEnd(LocalDateTime bookingWindowEnd) { this.bookingWindowEnd = bookingWindowEnd; return this; }
        public Builder maxStallsPerVendor(Integer maxStallsPerVendor) { this.maxStallsPerVendor = maxStallsPerVendor; return this; }
        public Builder approvalMode(ApprovalMode approvalMode) { this.approvalMode = approvalMode; return this; }
        public Builder allowedCategories(String allowedCategories) { this.allowedCategories = allowedCategories; return this; }
        public Builder rules(String rules) { this.rules = rules; return this; }
        public Builder coverColour(String coverColour) { this.coverColour = coverColour; return this; }

        public Event build() {
            Event event = new Event();
            event.setOrganizer(this.organizer);
            event.setName(this.name);
            event.setDescription(this.description);
            event.setCategory(this.category);
            event.setVenueName(this.venueName);
            event.setAddress(this.address);
            event.setCity(this.city);
            event.setCountry(this.country);
            event.setLatitude(this.latitude);
            event.setLongitude(this.longitude);
            event.setTimezone(this.timezone);
            event.setCurrency(this.currency);
            event.setStartDate(this.startDate);
            event.setEndDate(this.endDate);
            event.setStatus(this.status);
            event.setLayoutType(this.layoutType);
            event.setBookingWindowStart(this.bookingWindowStart);
            event.setBookingWindowEnd(this.bookingWindowEnd);
            event.setMaxStallsPerVendor(this.maxStallsPerVendor);
            event.setApprovalMode(this.approvalMode);
            event.setAllowedCategories(this.allowedCategories);
            event.setRules(this.rules);
            event.setCoverColour(this.coverColour);
            return event;
        }
    }
}
