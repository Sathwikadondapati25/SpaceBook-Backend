package com.spacebook.dto.profile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class VendorProfileRequest {

    @NotBlank(message = "Business name is required")
    @Size(max = 255)
    private String businessName;

    @NotBlank(message = "Contact name is required")
    @Size(max = 255)
    private String contactName;

    @NotBlank(message = "Phone is required")
    @Size(max = 20)
    private String phone;

    @NotBlank(message = "Category is required")
    @Size(max = 100)
    private String category;

    @NotBlank(message = "City is required")
    @Size(max = 100)
    private String city;

    @NotBlank(message = "Country is required")
    @Size(max = 100)
    private String country;

    private String description;

    private String website;

    private Integer yearsOperating;

    private String avatarUrl;

    public VendorProfileRequest() {
    }

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
}
