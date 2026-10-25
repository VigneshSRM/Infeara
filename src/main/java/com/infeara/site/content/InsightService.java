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
public class InsightService {
    private static final Logger log = LoggerFactory.getLogger(InsightService.class);
    private static final ParameterizedTypeReference<List<Map<String, Object>>> ARTICLE_LIST =
            new ParameterizedTypeReference<>() { };

    private final String supabaseUrl;
    private final String serviceRoleKey;
    private final RestClient.Builder restClientBuilder;

    private final List<Insight> previewArticles = List.of(
            new Insight("server-backup-strategy", "How to build a reliable server backup strategy",
                    "INFRASTRUCTURE", "A practical way to think about recovery points, restore testing, and ownership.",
                    "A backup plan is useful only when it matches what the business needs to recover. Begin by listing critical systems, the data they hold, and the people who depend on them.\n\nAgree recovery point and recovery time expectations with the business owners. These targets help determine how often data should be protected and how quickly services need to return.\n\nKeep backup copies isolated from the systems they protect, limit administrative access, and monitor backup jobs for failures. A successful job report is not the same as a verified restore.\n\nSchedule restore tests for representative systems and document the steps, dependencies, and responsibilities. Update the plan when applications, infrastructure, or business requirements change.",
                    "Infeara Editorial", "25 Oct 2026", 4),
            new Insight("patch-management-checklist", "Windows Server patch management: a planning checklist",
                    "INFRASTRUCTURE", "Coordinate updates with system owners, maintenance windows, and rollback plans.",
                    "Start with an accurate inventory of servers, applications, owners, and support dependencies. Classify systems by business criticality and understand which services depend on each one.\n\nReview vendor advisories and test updates in a representative non-production environment where possible. Identify application compatibility risks, disk-space requirements, and any prerequisite changes.\n\nAgree a maintenance window with affected stakeholders. Prepare a backup or recovery point, a validation checklist, communication steps, and a rollback approach before deployment.\n\nAfter patching, verify the operating system and the business application—not only that a server restarted. Record exceptions, outcomes, and follow-up work so the next maintenance cycle starts with better information.",
                    "Infeara Editorial", "25 Oct 2026", 4),
            new Insight("cloud-vs-on-premises", "Cloud vs. on-premises: a business decision framework",
                    "CLOUD & DATACENTER", "Compare workload needs, operating responsibilities, resilience, and total cost.",
                    "Cloud and on-premises infrastructure are operating choices, not a simple old-versus-new comparison. The right fit depends on the workload, its users, its data, and the team's ability to operate it.\n\nConsider demand patterns, performance requirements, data location, connectivity, recovery expectations, and integration with existing systems. Include the skills and operational processes needed after implementation.\n\nCompare total cost over a realistic planning period. Include migration, licensing, network connectivity, backup, monitoring, support, and future capacity—not only the initial server or monthly cloud bill.\n\nMany environments use a mix of platforms. Start with clear workload requirements, document the trade-offs, and review the decision as business priorities change.",
                    "Infeara Editorial", "25 Oct 2026", 4)
    );

    public InsightService(
            @Value("${supabase.url:}") String supabaseUrl,
            @Value("${supabase.service-role-key:}") String serviceRoleKey,
            RestClient.Builder restClientBuilder) {
        this.supabaseUrl = supabaseUrl == null ? "" : supabaseUrl.trim().replaceAll("/+$", "");
        this.serviceRoleKey = serviceRoleKey == null ? "" : serviceRoleKey.trim();
        this.restClientBuilder = restClientBuilder;
    }

    public List<Insight> findPublished() {
        if (supabaseUrl.isBlank() || serviceRoleKey.isBlank()) return previewArticles;
        try {
            List<Map<String, Object>> rows = client().get()
                    .uri(uriBuilder -> uriBuilder.path("/insights")
                            .queryParam("select", "slug,title,category,excerpt,content,author,published_at,reading_minutes")
                            .queryParam("status", "eq.published")
                            .queryParam("order", "published_at.desc")
                            .build())
                    .retrieve()
                    .body(ARTICLE_LIST);
            if (rows == null) return List.of();
            return rows.stream().map(this::toInsight).toList();
        } catch (RestClientException exception) {
            log.warn("Could not load published insights from Supabase; using the local editorial fallback.");
            return previewArticles;
        }
    }

    public Optional<Insight> findPublished(String slug) {
        return findPublished().stream().filter(article -> article.slug().equals(slug)).findFirst();
    }

    private RestClient client() {
        return restClientBuilder.clone()
                .baseUrl(supabaseUrl + "/rest/v1")
                .defaultHeader("apikey", serviceRoleKey)
                .defaultHeader("Authorization", "Bearer " + serviceRoleKey)
                .build();
    }

    private Insight toInsight(Map<String, Object> row) {
        return new Insight(
                text(row, "slug"), text(row, "title"), text(row, "category"), text(row, "excerpt"),
                text(row, "content"), text(row, "author"), text(row, "published_at"),
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
