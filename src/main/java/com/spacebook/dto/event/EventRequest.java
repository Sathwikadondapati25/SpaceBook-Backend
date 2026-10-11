package com.spacebook.dto.event;

import com.spacebook.entity.Event.ApprovalMode;
import com.spacebook.entity.Event.EventStatus;
import com.spacebook.entity.Event.LayoutType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class EventRequest {

    @NotBlank(message = "Event name is required")
    private String name;

    private String description;

    @NotBlank(message = "Category is required")
    private String category;

    @NotBlank(message = "Venue name is required")
    private String venueName;

    @NotBlank(message = "Address is required")
    private String address;

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "Country is required")
    private String country;

    @NotNull(message = "Latitude is required")
    private BigDecimal latitude;

    @NotNull(message = "Longitude is required")
    private BigDecimal longitude;

    @NotBlank(message = "Timezone is required")
    private String timezone;

    @NotBlank(message = "Currency is required")
    private String currency;

    @NotNull(message = "Start date is required")
    private LocalDateTime startDate;

    @NotNull(message = "End date is required")
    private LocalDateTime endDate;

    private EventStatus status;

    @NotNull(message = "Layout type is required")
    private LayoutType layoutType;

    private LocalDateTime bookingWindowStart;

    private LocalDateTime bookingWindowEnd;

    private Integer maxStallsPerVendor;

    private ApprovalMode approvalMode;

    private List<String> allowedCategories;

    private String rules;

    private String coverColour;

    public EventRequest() {
    }

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

    public List<String> getAllowedCategories() { return allowedCategories; }
    public void setAllowedCategories(List<String> allowedCategories) { this.allowedCategories = allowedCategories; }

    public String getRules() { return rules; }
    public void setRules(String rules) { this.rules = rules; }

    public String getCoverColour() { return coverColour; }
    public void setCoverColour(String coverColour) { this.coverColour = coverColour; }
}
