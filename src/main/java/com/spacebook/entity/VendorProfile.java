package com.spacebook.entity;

import jakarta.persistence.*;

/**
 * Vendor profile with business information
 */
@Entity
@Table(name = "vendor_profiles")
public class VendorProfile {

    @Id
    private Long userId;

    @Column(nullable = false, length = 255)
    private String businessName;

    @Column(name = "contact_name", nullable = false, length = 255)
    private String contactName;

    @Column(nullable = false, length = 20)
    private String phone;

    @Column(nullable = false, length = 100)
    private String category;

    @Column(nullable = false, length = 100)
    private String city;

    @Column(nullable = false, length = 100)
    private String country;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 500)
    private String website;

    @Column(name = "years_operating")
    private Integer yearsOperating;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    public VendorProfile() {
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getBusinessName() { return businessName; }
    public void setBusinessName(String businessName) { this.businessName = businessName; }

    public String getContactName() { return contactName; }
    public void setContactName(String contactName) { this.contactName = contactName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }

    public Integer getYearsOperating() { return yearsOperating; }
    public void setYearsOperating(Integer yearsOperating) { this.yearsOperating = yearsOperating; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long userId;
        private String businessName;
        private String contactName;
        private String phone;
        private String category;
        private String city;
        private String country;
        private String description;
        private String website;
        private Integer yearsOperating;
        private String avatarUrl;
        private User user;

        public Builder userId(Long userId) { this.userId = userId; return this; }
        public Builder businessName(String businessName) { this.businessName = businessName; return this; }
        public Builder contactName(String contactName) { this.contactName = contactName; return this; }
        public Builder phone(String phone) { this.phone = phone; return this; }
        public Builder category(String category) { this.category = category; return this; }
        public Builder city(String city) { this.city = city; return this; }
        public Builder country(String country) { this.country = country; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder website(String website) { this.website = website; return this; }
        public Builder yearsOperating(Integer yearsOperating) { this.yearsOperating = yearsOperating; return this; }
        public Builder avatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; return this; }
        public Builder user(User user) { this.user = user; return this; }

        public VendorProfile build() {
            VendorProfile p = new VendorProfile();
            p.setUserId(this.userId);
            p.setBusinessName(this.businessName);
            p.setContactName(this.contactName);
            p.setPhone(this.phone);
            p.setCategory(this.category);
            p.setCity(this.city);
            p.setCountry(this.country);
            p.setDescription(this.description);
            p.setWebsite(this.website);
            p.setYearsOperating(this.yearsOperating);
            p.setAvatarUrl(this.avatarUrl);
            p.setUser(this.user);
            return p;
        }
    }
}
