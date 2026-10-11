package com.spacebook.dto.profile;

import com.spacebook.entity.OrganizerProfile.OrgType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class OrganizerProfileRequest {

    @NotBlank(message = "Organization name is required")
    @Size(max = 255)
    private String organizationName;

    @NotBlank(message = "Contact name is required")
    @Size(max = 255)
    private String contactName;

    @NotBlank(message = "Phone is required")
    @Size(max = 20)
    private String phone;

    @NotNull(message = "Organization type is required")
    private OrgType orgType;

    @NotBlank(message = "City is required")
    @Size(max = 100)
    private String city;

    @NotBlank(message = "Country is required")
    @Size(max = 100)
    private String country;

    private String website;

    private String description;

    public OrganizerProfileRequest() {
    }

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
}
