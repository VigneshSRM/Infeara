package com.infeara.site.api;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class ConsultationService {
    private final String supabaseUrl;
    private final String serviceRoleKey;
    private final RestClient.Builder restClientBuilder;

    public ConsultationService(
            @Value("${supabase.url:}") String supabaseUrl,
            @Value("${supabase.service-role-key:}") String serviceRoleKey,
            RestClient.Builder restClientBuilder) {
        this.supabaseUrl = supabaseUrl == null ? "" : supabaseUrl.trim().replaceAll("/+$", "");
        this.serviceRoleKey = serviceRoleKey == null ? "" : serviceRoleKey.trim();
        this.restClientBuilder = restClientBuilder;
    }

    public void save(ConsultationRequest request) {
        if (supabaseUrl.isBlank() || serviceRoleKey.isBlank()) {
            throw new IllegalStateException("Supabase is not configured. Set SUPABASE_URL and SUPABASE_SERVICE_ROLE_KEY.");
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("full_name", request.fullName().trim());
        payload.put("business_email", request.businessEmail().trim());
        payload.put("company_name", request.companyName().trim());
        payload.put("role_department", blankToNull(request.roleDepartment()));
        payload.put("service_of_interest", request.serviceOfInterest().trim());
        payload.put("description", request.description().trim());
        payload.put("preferred_contact", blankToNull(request.preferredContact()));
        payload.put("landing_page", request.landingPage() == null || request.landingPage().isBlank()
                ? "/" : request.landingPage().trim());

        RestClient client = restClientBuilder.clone()
                .baseUrl(supabaseUrl + "/rest/v1")
                .defaultHeader("apikey", serviceRoleKey)
                .defaultHeader("Authorization", "Bearer " + serviceRoleKey)
                .build();

        client.post()
                .uri("/consultation_requests")
                .contentType(MediaType.APPLICATION_JSON)
                .body(payload)
                .retrieve()
                .toBodilessEntity();
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
