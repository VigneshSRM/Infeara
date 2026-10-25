package com.infeara.site.seo;

import java.time.Duration;

import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Serves {@code /robots.txt}. Public pages, CSS and JavaScript stay crawlable; the JSON API is
 * excluded because it is a utility endpoint rather than content. Deployments that must not be
 * indexed disallow crawling entirely instead of relying on the secrecy of the environment URL.
 */
@Controller
public class RobotsController {
    private final SeoProperties properties;

    public RobotsController(SeoProperties properties) {
        this.properties = properties;
    }

    @GetMapping(value = "/robots.txt", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> robots() {
        StringBuilder body = new StringBuilder();
        body.append("# robots.txt for ").append(SeoProperties.SITE_NAME).append('\n');
        body.append("User-agent: *\n");
        if (properties.isIndexable()) {
            body.append("Allow: /\n");
            body.append("Disallow: /api/\n");
            body.append('\n');
            body.append("Sitemap: ").append(properties.absolute("/sitemap.xml")).append('\n');
        } else {
            // Staging, preview and development deployments stay out of search results.
            body.append("Disallow: /\n");
            body.append('\n');
            body.append("# This environment is not indexable. Set SITE_INDEXABLE=true to allow crawling.\n");
        }
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_PLAIN)
                .cacheControl(CacheControl.maxAge(Duration.ofHours(1)).cachePublic())
                .body(body.toString());
    }
}
