package com.infeara.site.content;

public record Insight(
        String slug,
        String title,
        String category,
        String excerpt,
        String content,
        String author,
        String publishedAt,
        int readingMinutes
) {
    /** Fallback illustration when an article's category has no dedicated photograph. */
    private static final String DEFAULT_IMAGE = "editorial/insights-cover.jpg";

    /**
     * Photographs for the seeded articles, so two articles in the same category do not show the
     * same picture. Articles published later fall back to their category below.
     */
    private static final java.util.Map<String, String> ARTICLE_IMAGES = java.util.Map.of(
            "server-backup-strategy", "services/infrastructure-overview.jpg",
            "patch-management-checklist", "services/managed-it-overview.jpg",
            "cloud-vs-on-premises", "services/cloud-datacenter-overview.jpg");

    /**
     * Photograph used by the field-note cards. Chosen from the article's own category rather than
     * stored per article, so newly published articles get a fitting image without extra data.
     */
    public String imageFileName() {
        String specific = slug == null ? null : ARTICLE_IMAGES.get(slug);
        if (specific != null) {
            // Generated per article by scripts/generate-page-images.py, so the three field notes do
            // not repeat the photographs used on the service pages.
            return "editorial/pages/" + slug + ".webp";
        }
        String key = category == null ? "" : category.toLowerCase(java.util.Locale.ROOT);
        if (key.contains("cyber") || key.contains("secur")) {
            return "services/cybersecurity-overview.jpg";
        }
        if (key.contains("cloud") || key.contains("datacenter") || key.contains("datacentre")) {
            return "services/cloud-datacenter-overview.jpg";
        }
        if (key.contains("network")) {
            return "services/infrastructure-detail.jpg";
        }
        if (key.contains("managed") || key.contains("operations")) {
            return "services/managed-it-overview.jpg";
        }
        if (key.contains("outsourc") || key.contains("support")) {
            return "services/it-outsourcing-overview.jpg";
        }
        if (key.contains("infrastructure") || key.contains("backup") || key.contains("server")) {
            return "services/infrastructure-overview.jpg";
        }
        return DEFAULT_IMAGE;
    }
}
