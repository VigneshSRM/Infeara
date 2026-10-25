package com.infeara.site.seo;

import java.util.List;

/**
 * Minimal JSON-LD writer for schema.org markup.
 *
 * <p>Only accurate, page-visible facts are emitted: no invented ratings, offers, addresses,
 * certifications or review counts. Strings are escaped for safe embedding inside a
 * {@code <script type="application/ld+json">} block.</p>
 */
public final class JsonLd {
    private static final String ORGANIZATION_ID_SUFFIX = "/#organization";
    private static final String WEB_SITE_ID_SUFFIX = "/#website";

    private JsonLd() {
    }

    public static String organization(String siteUrl, String logoUrl) {
        return "{\"@type\":\"Organization\",\"@id\":" + string(siteUrl + ORGANIZATION_ID_SUFFIX)
                + ",\"name\":" + string(SeoProperties.SITE_NAME)
                + ",\"url\":" + string(siteUrl)
                + ",\"logo\":{\"@type\":\"ImageObject\",\"url\":" + string(logoUrl) + "}"
                + "}";
    }

    public static String webSite(String siteUrl) {
        return "{\"@type\":\"WebSite\",\"@id\":" + string(siteUrl + WEB_SITE_ID_SUFFIX)
                + ",\"name\":" + string(SeoProperties.SITE_NAME)
                + ",\"url\":" + string(siteUrl)
                + ",\"publisher\":{\"@id\":" + string(siteUrl + ORGANIZATION_ID_SUFFIX) + "}"
                + ",\"inLanguage\":\"en\""
                + "}";
    }

    /** {@code BreadcrumbList} mirroring the breadcrumb trail that is visible on the page. */
    public static String breadcrumbs(List<Breadcrumb> crumbs) {
        StringBuilder items = new StringBuilder();
        int position = 1;
        for (Breadcrumb crumb : crumbs) {
            if (crumb == null || crumb.name() == null || crumb.name().isBlank()) {
                continue;
            }
            if (items.length() > 0) {
                items.append(',');
            }
            items.append("{\"@type\":\"ListItem\",\"position\":").append(position++)
                    .append(",\"name\":").append(string(crumb.name()));
            if (crumb.url() != null && !crumb.url().isBlank()) {
                items.append(",\"item\":").append(string(crumb.url()));
            }
            items.append('}');
        }
        return "{\"@type\":\"BreadcrumbList\",\"itemListElement\":[" + items + "]}";
    }

    public static String service(String name, String description, String pageUrl, String siteUrl, String serviceType) {
        StringBuilder node = new StringBuilder("{\"@type\":\"Service\"");
        node.append(",\"name\":").append(string(name));
        if (description != null && !description.isBlank()) {
            node.append(",\"description\":").append(string(description));
        }
        node.append(",\"url\":").append(string(pageUrl));
        if (serviceType != null && !serviceType.isBlank()) {
            node.append(",\"serviceType\":").append(string(serviceType));
        }
        node.append(",\"provider\":{\"@type\":\"Organization\",\"name\":").append(string(SeoProperties.SITE_NAME))
                .append(",\"url\":").append(string(siteUrl)).append('}');
        node.append('}');
        return node.toString();
    }

    /**
     * Article markup for authored blog and newsletter content. Date fields are only emitted when a
     * real publication date is available, and the authoring organisation always points at the site
     * root; nothing is invented when the source omits a value.
     */
    public static String article(String type, String headline, String description, String pageUrl,
                                 String imageUrl, String author, String authorUrl, String publishedIso) {
        StringBuilder node = new StringBuilder("{\"@type\":").append(string(isBlank(type) ? "BlogPosting" : type));
        node.append(",\"headline\":").append(string(headline));
        if (!isBlank(description)) {
            node.append(",\"description\":").append(string(description));
        }
        node.append(",\"mainEntityOfPage\":{\"@type\":\"WebPage\",\"@id\":").append(string(pageUrl)).append('}');
        node.append(",\"url\":").append(string(pageUrl));
        if (!isBlank(imageUrl)) {
            node.append(",\"image\":").append(string(imageUrl));
        }
        if (!isBlank(publishedIso)) {
            node.append(",\"datePublished\":").append(string(publishedIso));
            node.append(",\"dateModified\":").append(string(publishedIso));
        }
        if (!isBlank(author)) {
            node.append(",\"author\":{\"@type\":\"Organization\",\"name\":").append(string(author));
            if (!isBlank(authorUrl)) {
                node.append(",\"url\":").append(string(authorUrl));
            }
            node.append('}');
        }
        node.append('}');
        return node.toString();
    }

    /** Wraps one or more schema nodes in a single {@code @graph} document. */
    public static String graph(String... nodes) {
        StringBuilder json = new StringBuilder("{\"@context\":\"https://schema.org\",\"@graph\":[");
        boolean first = true;
        for (String node : nodes) {
            if (node == null || node.isBlank()) {
                continue;
            }
            if (!first) {
                json.append(',');
            }
            json.append(node);
            first = false;
        }
        return json.append("]}").toString();
    }

    public static String string(String value) {
        return "\"" + escape(value) + "\"";
    }

    /**
     * Escapes text for a JSON string literal. Characters that could break out of an inline script
     * block are escaped as unicode sequences, which JSON parsers read back as the original text.
     */
    public static String escape(String value) {
        if (value == null) {
            return "";
        }
        StringBuilder escaped = new StringBuilder(value.length() + 16);
        for (int index = 0; index < value.length(); index++) {
            char character = value.charAt(index);
            switch (character) {
                case '"' -> escaped.append("\\\"");
                case '\\' -> escaped.append("\\\\");
                case '\b' -> escaped.append("\\b");
                case '\f' -> escaped.append("\\f");
                case '\n' -> escaped.append("\\n");
                case '\r' -> escaped.append("\\r");
                case '\t' -> escaped.append("\\t");
                case '<' -> escaped.append("\\u003c");
                case '>' -> escaped.append("\\u003e");
                case '&' -> escaped.append("\\u0026");
                case '\u2028' -> escaped.append("\\u2028");
                case '\u2029' -> escaped.append("\\u2029");
                default -> {
                    if (character < 0x20) {
                        escaped.append(String.format("\\u%04x", (int) character));
                    } else {
                        escaped.append(character);
                    }
                }
            }
        }
        return escaped.toString();
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
