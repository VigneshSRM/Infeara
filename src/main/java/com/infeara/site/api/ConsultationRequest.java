package com.infeara.site.api;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * A consultation enquiry.
 *
 * <p>The record stays immutable: submitted values are bound through the canonical constructor.
 * JavaBean-style getters are provided as well because Spring's bean wrapper, and therefore
 * Thymeleaf's {@code th:field}, resolves values through {@code getX()}/{@code isX()} accessors
 * rather than record accessors.</p>
 */
public record ConsultationRequest(
        @NotBlank @Size(min = 2, max = 120) String fullName,
        @NotBlank @Email @Size(max = 254) String businessEmail,
        @NotBlank @Size(max = 160) String companyName,
        @Size(max = 120) String roleDepartment,
        @NotBlank @Size(min = 2, max = 120) String serviceOfInterest,
        @NotBlank @Size(min = 10, max = 3000) String description,
        @Size(max = 40) String preferredContact,
        @Size(max = 500) String landingPage,
        @Size(max = 200) String website,
        @AssertTrue boolean consent
) {
    public String getFullName() { return fullName; }

    public String getBusinessEmail() { return businessEmail; }

    public String getCompanyName() { return companyName; }

    public String getRoleDepartment() { return roleDepartment; }

    public String getServiceOfInterest() { return serviceOfInterest; }

    public String getDescription() { return description; }

    public String getPreferredContact() { return preferredContact; }

    public String getLandingPage() { return landingPage; }

    public String getWebsite() { return website; }

    public boolean isConsent() { return consent; }
}
