package com.infeara.site.seo;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;

/**
 * Builds the per-page SEO values that the shared Thymeleaf head fragment renders: canonical URL,
 * robots directive, social metadata and schema.org JSON-LD.
 */
@Service
public class SeoService {
    private static final String LOGO_PATH = "/assets/brand/infeara-logo-460.png";
    private static final DateTimeFormatter DISPLAY_DATE_FORMAT =
            DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH);

    private final SeoProperties properties;

    public SeoService(SeoProperties properties) {
        this.properties = properties;
    }

    /** Absolute canonical URL for a site-root relative path; query strings and fragments are dropped. */
    public String canonical(String path) {
        return properties.absolute(path);
    }

    public String socialImage() {
        return properties.defaultSocialImage();
    }

    public String logoImage() {
        return properties.absolute(LOGO_PATH);
    }

    public String robotsDirective() {
        return properties.robotsDirective();
    }

    public Breadcrumb crumb(String name, String path) {
        return new Breadcrumb(name, path == null || path.isBlank() ? null : properties.absolute(path));
    }

    public String breadcrumbGraph(List<Breadcrumb> crumbs) {
        return JsonLd.graph(JsonLd.breadcrumbs(crumbs));
    }

    public String homeGraph() {
        return JsonLd.graph(
                JsonLd.organization(properties.baseUrl(), logoImage()),
                JsonLd.webSite(properties.baseUrl()));
    }

    public String organizationAndBreadcrumbGraph(List<Breadcrumb> crumbs) {
        return JsonLd.graph(
                JsonLd.organization(properties.baseUrl(), logoImage()),
                JsonLd.breadcrumbs(crumbs));
    }

    public String serviceGraph(List<Breadcrumb> crumbs, String name, String description, String pageUrl) {
        return JsonLd.graph(
                JsonLd.breadcrumbs(crumbs),
                JsonLd.service(name, description, pageUrl, properties.baseUrl(), name));
    }

    public String articleGraph(List<Breadcrumb> crumbs, String type, String headline, String description,
                               String pageUrl, String publishedDisplayDate, String author) {
        return JsonLd.graph(
                JsonLd.breadcrumbs(crumbs),
                JsonLd.article(type, headline, description, pageUrl, socialImage(), author,
                        properties.baseUrl(), isoDate(publishedDisplayDate)));
    }

    /**
     * Converts the displayed publication date into an ISO-8601 date for structured data. Returns an
     * empty string when the value cannot be verified, so no date is invented.
     */
    public String isoDate(String displayDate) {
        if (displayDate == null || displayDate.isBlank()) {
            return "";
        }
        String value = displayDate.trim();
        try {
            return LocalDate.parse(value).toString();
        } catch (RuntimeException ignored) {
            // Fall through to the display format used by the editorial content.
        }
        try {
            return LocalDate.parse(value, DISPLAY_DATE_FORMAT).toString();
        } catch (RuntimeException ignored) {
            return "";
        }
    }
}
