package com.infeara.site.seo;

import java.time.Duration;
import java.util.LinkedHashSet;
import java.util.Set;

import com.infeara.site.content.InsightService;
import com.infeara.site.content.NewsletterService;
import com.infeara.site.content.ServicePage;
import com.infeara.site.content.SpecialistServicePage;
import com.infeara.site.content.WebsiteContent;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Generates {@code /sitemap.xml} from published, canonical, indexable content only.
 *
 * <p>Redirecting, missing, private or duplicated URLs are never listed: legacy {@code .html}
 * routes are omitted, and article detail pages are listed once at their canonical
 * {@code /blogs/{slug}} address rather than at both {@code /blogs} and {@code /insights}.</p>
 */
@Controller
public class SitemapController {
    private final WebsiteContent content;
    private final InsightService insightService;
    private final NewsletterService newsletterService;
    private final SeoProperties properties;
    private final SeoService seoService;

    public SitemapController(WebsiteContent content, InsightService insightService,
                             NewsletterService newsletterService, SeoProperties properties,
                             SeoService seoService) {
        this.content = content;
        this.insightService = insightService;
        this.newsletterService = newsletterService;
        this.properties = properties;
        this.seoService = seoService;
    }

    @GetMapping(value = "/sitemap.xml", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<String> sitemap() {
        StringBuilder xml = new StringBuilder(4096);
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n");

        for (String path : staticPaths()) {
            appendUrl(xml, path, null);
        }
        for (ServicePage service : content.services()) {
            appendUrl(xml, "/services/" + service.slug(), null);
        }
        for (SpecialistServicePage specialist : content.allSpecialistServices()) {
            appendUrl(xml, "/services/" + specialist.parentSlug() + "/" + specialist.slug(), null);
        }
        for (var industry : content.industries()) {
            appendUrl(xml, "/industries/" + industry.slug(), null);
        }
        for (var article : insightService.findPublished()) {
            appendUrl(xml, "/blogs/" + article.slug(), seoService.isoDate(article.publishedAt()));
        }
        for (var edition : newsletterService.findPublished()) {
            appendUrl(xml, "/newsletters/" + edition.slug(), seoService.isoDate(edition.publishedAt()));
        }
        xml.append("</urlset>\n");

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_XML)
                .cacheControl(CacheControl.maxAge(Duration.ofHours(1)).cachePublic())
                .header("X-Robots-Tag", properties.isIndexable() ? "index,follow" : "noindex, nofollow")
                .body(xml.toString());
    }

    private Set<String> staticPaths() {
        Set<String> paths = new LinkedHashSet<>();
        paths.add("/");
        paths.add("/services");
        paths.add("/industries");
        paths.add("/about");
        paths.add("/clients");
        paths.add("/careers");
        paths.add("/careers/open-positions");
        paths.add("/blogs");
        paths.add("/insights");
        paths.add("/newsletters");
        paths.add("/contact");
        paths.add("/privacy");
        paths.add("/terms");
        return paths;
    }

    private void appendUrl(StringBuilder xml, String path, String lastModifiedIso) {
        xml.append("  <url><loc>").append(escape(properties.absolute(path))).append("</loc>");
        if (lastModifiedIso != null && !lastModifiedIso.isBlank()) {
            xml.append("<lastmod>").append(lastModifiedIso).append("</lastmod>");
        }
        xml.append("</url>\n");
    }

    private String escape(String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
