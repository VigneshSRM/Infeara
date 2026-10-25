package com.infeara.site.api;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

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
) { }
