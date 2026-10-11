package com.spacebook.entity;

import jakarta.persistence.*;

/**
 * Organizer profile with organization information
 */
@Entity
@Table(name = "organizer_profiles")
public class OrganizerProfile {

    @Id
    private Long userId;

    @Column(name = "organization_name", nullable = false, length = 255)
    private String organizationName;

    @Column(name = "contact_name", nullable = false, length = 255)
    private String contactName;

    @Column(nullable = false, length = 20)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(name = "org_type", nullable = false)
    private OrgType orgType;

    @Column(nullable = false, length = 100)
    private String city;

    @Column(nullable = false, length = 100)
    private String country;

    @Column(length = 500)
    private String website;

    @Column(columnDefinition = "TEXT")
    private String description;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    public OrganizerProfile() {
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getOrganizationName() { return organizationName; }
    public void setOrganizationName(String organizationName) { this.organizationName = organizationName; }

    public String getContactName() { return contactName; }
    public void setContactName(String contactName) { this.contactName = contactName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public OrgType getOrgType() { return orgType; }
    public void setOrgType(OrgType orgType) { this.orgType = orgType; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long userId;
        private String organizationName;
        private String contactName;
        private String phone;
        private OrgType orgType;
        private String city;
        private String country;
        private String website;
        private String description;
        private User user;

        public Builder userId(Long userId) { this.userId = userId; return this; }
        public Builder organizationName(String organizationName) { this.organizationName = organizationName; return this; }
        public Builder contactName(String contactName) { this.contactName = contactName; return this; }
        public Builder phone(String phone) { this.phone = phone; return this; }
        public Builder orgType(OrgType orgType) { this.orgType = orgType; return this; }
        public Builder city(String city) { this.city = city; return this; }
        public Builder country(String country) { this.country = country; return this; }
        public Builder website(String website) { this.website = website; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder user(User user) { this.user = user; return this; }

        public OrganizerProfile build() {
            OrganizerProfile p = new OrganizerProfile();
            p.setUserId(this.userId);
            p.setOrganizationName(this.organizationName);
            p.setContactName(this.contactName);
            p.setPhone(this.phone);
            p.setOrgType(this.orgType);
            p.setCity(this.city);
            p.setCountry(this.country);
            p.setWebsite(this.website);
            p.setDescription(this.description);
            p.setUser(this.user);
            return p;
        }
    }

    public enum OrgType {
        COLLEGE,
        COMPANY,
        NGO,
        INDIVIDUAL
    }
}
