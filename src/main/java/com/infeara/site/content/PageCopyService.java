package com.infeara.site.content;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

/**
 * Loads the authored page copy from {@code content/page-copy*.txt}.
 *
 * <p>The files use a deliberately small, review-friendly format:</p>
 * <pre>
 * &#64;capability cloud-datacenter/cloud-migration-planning
 * title: Cloud Migration Planning &amp; Sequencing
 * description: One sentence for search results.
 * intro: The paragraph shown on the page.
 * covers: first item | second item | third item
 * faq: Question? | Answer.
 * </pre>
 *
 * <p>Entries are validated as they load: a missing key, a missing required field, or a title that
 * duplicates another page fails at startup rather than publishing a thin or duplicated page.</p>
 */
@Service
public class PageCopyService {
    private static final String CONTENT_PATTERN = "classpath*:content/page-copy*.txt";
    private static final Set<String> KNOWN_FIELDS =
            Set.of("title", "description", "intro", "covers", "signs", "deliverables", "faq");

    private final Map<String, PageCopy> copies;

    public PageCopyService(WebsiteContent content) {
        this.copies = load();
        validate(content);
    }

    /** Copy for a static route such as {@code /about}. */
    public PageCopy forPath(String path) {
        return copies.get("page:" + path);
    }

    /** Copy for a service area, keyed by its slug. */
    public PageCopy forService(String slug) {
        return copies.get("service:" + slug);
    }

    /** Copy for a capability page, keyed by {@code parentSlug/slug}. */
    public PageCopy forCapability(String parentSlug, String slug) {
        return copies.get("capability:" + parentSlug + "/" + slug);
    }

    /** Copy for an industry page, keyed by its slug. */
    public PageCopy forIndustry(String slug) {
        return copies.get("industry:" + slug);
    }

    public int size() {
        return copies.size();
    }

    private Map<String, PageCopy> load() {
        Map<String, Builder> builders = new LinkedHashMap<>();
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        List<Resource> files;
        try {
            files = List.of(resolver.getResources(CONTENT_PATTERN));
        } catch (IOException exception) {
            throw new UncheckedIOException("Could not read the page copy files", exception);
        }
        if (files.isEmpty()) {
            throw new IllegalStateException("No page copy files found at " + CONTENT_PATTERN);
        }
        for (Resource file : files) {
            readInto(builders, file);
        }
        Map<String, PageCopy> result = new LinkedHashMap<>();
        builders.forEach((key, builder) -> result.put(key, builder.build(key)));
        return result;
    }

    private void readInto(Map<String, Builder> builders, Resource file) {
        String content;
        try {
            content = file.getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new UncheckedIOException("Could not read " + file.getFilename(), exception);
        }
        Builder current = null;
        String field = null;
        StringBuilder value = new StringBuilder();

        for (String rawLine : content.split("\\R", -1)) {
            String line = rawLine.strip();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            if (line.startsWith("@")) {
                if (current != null) {
                    current.add(field, value);
                }
                String[] parts = line.substring(1).split("\\s+", 2);
                if (parts.length < 2) {
                    throw new IllegalStateException("Malformed entry header in " + file.getFilename() + ": " + line);
                }
                current = builders.computeIfAbsent(parts[0] + ":" + parts[1].strip(), key -> new Builder(parts[0]));
                field = null;
                value.setLength(0);
                continue;
            }
            int colon = line.indexOf(':');
            if (colon > 0 && KNOWN_FIELDS.contains(line.substring(0, colon).strip())) {
                if (current != null) {
                    current.add(field, value);
                }
                field = line.substring(0, colon).strip();
                value.setLength(0);
                value.append(line.substring(colon + 1).strip());
                continue;
            }
            // Continuation of the previous field, wrapped for readability in the source file.
            if (current == null || field == null) {
                throw new IllegalStateException("Text outside an entry in " + file.getFilename() + ": " + line);
            }
            value.append(' ').append(line);
        }
        if (current != null) {
            current.add(field, value);
        }
    }

    /** Fails fast when authored copy is missing or duplicated, so no page ships thin. */
    private void validate(WebsiteContent content) {
        Set<String> missing = new LinkedHashSet<>();
        List<String> problems = new ArrayList<>();

        content.services().forEach(service -> {
            if (forService(service.slug()) == null) {
                missing.add("service:" + service.slug());
            }
        });
        content.allSpecialistServices().forEach(page -> {
            if (forCapability(page.parentSlug(), page.slug()) == null) {
                missing.add("capability:" + page.parentSlug() + "/" + page.slug());
            }
        });
        content.industries().forEach(industry -> {
            if (forIndustry(industry.slug()) == null) {
                missing.add("industry:" + industry.slug());
            }
        });
        for (String path : STATIC_PATHS) {
            if (forPath(path) == null) {
                missing.add("page:" + path);
            }
        }
        if (!missing.isEmpty()) {
            throw new IllegalStateException("Missing page copy for: " + String.join(", ", missing));
        }

        Map<String, String> titles = new LinkedHashMap<>();
        Map<String, String> descriptions = new LinkedHashMap<>();
        copies.forEach((key, copy) -> {
            if (copy.seoTitle().isBlank()) {
                problems.add(key + " has no title");
            } else if (copy.seoTitle().length() > 62) {
                problems.add(key + " title is " + copy.seoTitle().length() + " characters");
            }
            String previousTitle = titles.put(copy.seoTitle().toLowerCase(java.util.Locale.ROOT), key);
            if (previousTitle != null) {
                problems.add(key + " reuses the title of " + previousTitle);
            }
            if (copy.metaDescription().isBlank()) {
                problems.add(key + " has no description");
            } else if (copy.metaDescription().length() > 165) {
                problems.add(key + " description is " + copy.metaDescription().length() + " characters");
            }
            String previousDescription = descriptions.put(copy.metaDescription(), key);
            if (previousDescription != null) {
                problems.add(key + " reuses the description of " + previousDescription);
            }
        });
        if (!problems.isEmpty()) {
            throw new IllegalStateException("Page copy problems: " + String.join("; ", problems));
        }
    }

    private static final List<String> STATIC_PATHS = List.of(
            "/", "/services", "/industries", "/about", "/clients", "/careers", "/careers/open-positions",
            "/blogs", "/insights", "/newsletters", "/contact", "/privacy", "/terms");

    /** Accumulates one entry while its file is read. */
    private static final class Builder {
        private final String type;
        private final Map<String, List<String>> fields = new LinkedHashMap<>();

        private Builder(String type) {
            this.type = type;
        }

        private void add(String field, StringBuilder value) {
            if (field == null || value.isEmpty()) {
                return;
            }
            fields.computeIfAbsent(field, key -> new ArrayList<>()).add(value.toString().strip());
        }

        private PageCopy build(String key) {
            List<String> covers = split(fields.get("covers"));
            List<String> signs = split(fields.get("signs"));
            List<String> deliverables = split(fields.get("deliverables"));
            List<PageCopy.Faq> faqs = new ArrayList<>();
            for (String raw : fields.getOrDefault("faq", List.of())) {
                int pipe = raw.indexOf('|');
                if (pipe < 0) {
                    throw new IllegalStateException("Malformed faq in " + key + ": " + raw);
                }
                faqs.add(new PageCopy.Faq(raw.substring(0, pipe).strip(), raw.substring(pipe + 1).strip()));
            }
            boolean isPage = "page".equals(type);
            if (!isPage && covers.size() < 3) {
                throw new IllegalStateException(key + " needs at least three 'covers' items");
            }
            if (!isPage && faqs.size() < 2) {
                throw new IllegalStateException(key + " needs at least two faqs");
            }
            return new PageCopy(
                    first(fields.get("title")),
                    first(fields.get("description")),
                    first(fields.get("intro")),
                    covers, signs, deliverables, List.copyOf(faqs));
        }

        private String first(List<String> values) {
            return values == null || values.isEmpty() ? "" : values.get(0);
        }

        private List<String> split(List<String> values) {
            if (values == null || values.isEmpty()) {
                return List.of();
            }
            return values.stream()
                    .flatMap(value -> java.util.Arrays.stream(value.split("\\|")))
                    .map(String::strip)
                    .filter(item -> !item.isEmpty())
                    .collect(Collectors.toList());
        }
    }
}
