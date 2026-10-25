package com.infeara.site.content;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Service
public class NewsletterService {
    private static final Logger log = LoggerFactory.getLogger(NewsletterService.class);
    private static final ParameterizedTypeReference<List<Map<String, Object>>> EDITION_LIST =
            new ParameterizedTypeReference<>() { };

    private final String supabaseUrl;
    private final String serviceRoleKey;
    private final RestClient.Builder restClientBuilder;

    public NewsletterService(
            @Value("${supabase.url:}") String supabaseUrl,
            @Value("${supabase.service-role-key:}") String serviceRoleKey,
            RestClient.Builder restClientBuilder) {
        this.supabaseUrl = supabaseUrl == null ? "" : supabaseUrl.trim().replaceAll("/+$", "");
        this.serviceRoleKey = serviceRoleKey == null ? "" : serviceRoleKey.trim();
        this.restClientBuilder = restClientBuilder;
    }

    public List<NewsletterEdition> findPublished() {
        if (supabaseUrl.isBlank() || serviceRoleKey.isBlank()) return List.of();
        try {
            List<Map<String, Object>> rows = client().get()
                    .uri(uriBuilder -> uriBuilder.path("/newsletter_editions")
                            .queryParam("select", "slug,edition_label,title,excerpt,content,published_at,reading_minutes")
                            .queryParam("status", "eq.published")
                            .queryParam("order", "published_at.desc")
                            .build())
                    .retrieve()
                    .body(EDITION_LIST);
            if (rows == null) return List.of();
            return rows.stream().map(this::toEdition).toList();
        } catch (RestClientException exception) {
            log.warn("Could not load published newsletter editions from Supabase; showing the empty archive.");
            return List.of();
        }
    }

    public Optional<NewsletterEdition> findPublished(String slug) {
        return findPublished().stream().filter(edition -> edition.slug().equals(slug)).findFirst();
    }

    private RestClient client() {
        return restClientBuilder.clone()
                .baseUrl(supabaseUrl + "/rest/v1")
                .defaultHeader("apikey", serviceRoleKey)
                .defaultHeader("Authorization", "Bearer " + serviceRoleKey)
                .build();
    }

    private NewsletterEdition toEdition(Map<String, Object> row) {
        return new NewsletterEdition(
                text(row, "slug"), text(row, "edition_label"), text(row, "title"),
                text(row, "excerpt"), text(row, "content"), text(row, "published_at"),
                row.get("reading_minutes") instanceof Number value ? value.intValue() : 1);
    }

    private String text(Map<String, Object> row, String key) {
        Object value = row.get(key);
        if (value == null) return "";
        String text = value.toString();
        if (key.equals("published_at") && text.length() >= 10) {
            try {
                return LocalDate.parse(text.substring(0, 10))
                        .format(DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH));
            } catch (RuntimeException ignored) {
                return text.substring(0, 10);
            }
        }
        return text;
    }
}
