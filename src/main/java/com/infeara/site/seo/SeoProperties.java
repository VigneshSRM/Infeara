package com.infeara.site.seo;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Environment level SEO configuration.
 *
 * <p>The canonical origin always comes from configuration ({@code seo.base-url}, normally supplied
 * through {@code SITE_BASE_URL}) and never from the request {@code Host} header, so a spoofed host
 * cannot change the canonical URL a crawler is told to use.</p>
 *
 * <p>{@code seo.indexable} ({@code SITE_INDEXABLE}) decides whether this deployment may appear in
 * search results. When it is not set, a deployment is indexable only when the canonical origin is
 * served over HTTPS, which keeps local and staging environments out of search results.</p>
 */
@Component
public class SeoProperties {
    public static final String SITE_NAME = "Infeara Technologies";
    public static final String DEFAULT_OG_IMAGE_PATH = "/assets/social/infeara-social-card.png";

    private final String baseUrl;
    private final boolean indexable;

    public SeoProperties(
            @Value("${seo.base-url:http://localhost:8080}") String baseUrl,
            @Value("${seo.indexable:}") String indexableOverride) {
        String resolvedBaseUrl = baseUrl == null ? "" : baseUrl.trim();
        if (resolvedBaseUrl.isEmpty()) {
            resolvedBaseUrl = "http://localhost:8080";
        }
        this.baseUrl = resolvedBaseUrl.replaceAll("/+$", "");

        String override = indexableOverride == null ? "" : indexableOverride.trim().toLowerCase(Locale.ROOT);
        if ("true".equals(override)) {
            this.indexable = true;
        } else if ("false".equals(override)) {
            this.indexable = false;
        } else {
            this.indexable = this.baseUrl.startsWith("https://");
        }
    }

    /** Canonical origin without a trailing slash, for example {@code https://www.example.com}. */
    public String baseUrl() {
        return baseUrl;
    }

    public boolean isIndexable() {
        return indexable;
    }

    public String siteName() {
        return SITE_NAME;
    }

    /** Absolute URL for a site-root relative path. The path is normalised and never trusted with a query string. */
    public String absolute(String path) {
        String normalized = path == null ? "" : path.trim();
        if (normalized.isEmpty()) {
            normalized = "/";
        }
        if (!normalized.startsWith("/")) {
            normalized = "/" + normalized;
        }
        int queryIndex = normalized.indexOf('?');
        if (queryIndex >= 0) {
            normalized = normalized.substring(0, queryIndex);
        }
        int fragmentIndex = normalized.indexOf('#');
        if (fragmentIndex >= 0) {
            normalized = normalized.substring(0, fragmentIndex);
        }
        if (normalized.length() > 1 && normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return baseUrl + normalized;
    }

    /** {@code index,follow} for public deployments; {@code noindex,nofollow} while staging or in development. */
    public String robotsDirective() {
        return indexable ? "index,follow" : "noindex,nofollow";
    }

    public String defaultSocialImage() {
        return absolute(DEFAULT_OG_IMAGE_PATH);
    }
}
