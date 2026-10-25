package com.infeara.site.seo;

import java.util.Locale;

/**
 * Small helpers that keep titles and descriptions unique, readable and inside the editorial
 * length ranges from the SEO standard (about 50-60 characters for a title and 140-160 for a
 * description). These are editorial starting points, not hard limits: copy is never padded with
 * filler and never cut in the middle of a word.
 */
public final class SeoText {
    private static final int TITLE_TARGET = 60;
    private static final int DESCRIPTION_MAX = 160;
    private static final int DESCRIPTION_MIN = 100;
    private static final String[] SENTENCE_ENDINGS = {". ", "! ", "? "};

    private SeoText() {
    }

    public static String clean(String value) {
        return value == null ? "" : value.replaceAll("\\s+", " ").trim();
    }

    /** Page title with the brand suffix, shortened only when the full title would be unwieldy. */
    public static String title(String label) {
        String primary = clean(label);
        String full = primary + " | " + SeoProperties.SITE_NAME;
        if (full.length() <= TITLE_TARGET) {
            return full;
        }
        String shorter = primary + " | Infeara";
        if (shorter.length() <= TITLE_TARGET) {
            return shorter;
        }
        return primary;
    }

    /** Page title with an explicit, page specific suffix. */
    public static String title(String label, String suffix) {
        String primary = clean(label);
        String ending = clean(suffix);
        return ending.isEmpty() ? primary : primary + " | " + ending;
    }

    /**
     * Page title with a page specific suffix, falling back to the shorter brand suffix when the
     * combination would be cut off in search results (long article and edition titles).
     */
    public static String titleWithSuffix(String label, String suffix) {
        String primary = clean(label);
        String ending = clean(suffix);
        if (ending.isEmpty()) {
            return primary;
        }
        String combined = primary + " | " + ending;
        return combined.length() <= TITLE_TARGET + 6 ? combined : title(primary);
    }

    /** Description from a single, already complete piece of copy. */
    public static String description(String text) {
        return truncate(clean(text), DESCRIPTION_MAX);
    }

    /**
     * Description built from the page's own summary plus optional supporting context.
     *
     * <p>Whole sentences are preferred over a hard character cut: if the full copy is too long,
     * the lead sentences are tried next, and only then does the text get shortened at a word or
     * sentence boundary. Nothing is invented or repeated to reach a target length.</p>
     */
    public static String description(String primary, String supporting) {
        String first = clean(primary);
        String second = clean(supporting);
        if (first.isEmpty() && second.isEmpty()) {
            return "";
        }
        if (first.isEmpty()) {
            return truncate(second, DESCRIPTION_MAX);
        }
        if (second.isEmpty()) {
            return truncate(first, DESCRIPTION_MAX);
        }

        String full = first + " " + second;
        if (full.length() <= DESCRIPTION_MAX) {
            return full;
        }

        String leadPair = leadSentence(first) + " " + leadSentence(second);
        if (leadPair.length() <= DESCRIPTION_MAX && leadPair.length() >= DESCRIPTION_MIN) {
            return leadPair;
        }

        String leadThenFull = leadSentence(first) + " " + second;
        if (leadThenFull.length() <= DESCRIPTION_MAX && leadThenFull.length() > leadPair.length()) {
            return leadThenFull;
        }

        return truncate(full, DESCRIPTION_MAX);
    }

    /**
     * Shortens text to the given length. A complete sentence is preferred; otherwise the cut lands
     * on a word boundary and drops a dangling conjunction, article or punctuation mark.
     */
    public static String truncate(String value, int maxLength) {
        String text = clean(value);
        if (text.length() <= maxLength) {
            return text;
        }

        String head = text.substring(0, maxLength);
        int sentenceEnd = -1;
        for (String ending : SENTENCE_ENDINGS) {
            int index = head.lastIndexOf(ending);
            if (index > sentenceEnd) {
                sentenceEnd = index;
            }
        }
        if (sentenceEnd >= maxLength * 3 / 5) {
            return head.substring(0, sentenceEnd + 1).trim();
        }

        int cut = text.lastIndexOf(' ', maxLength);
        if (cut < maxLength / 2) {
            cut = maxLength;
        }
        String trimmed = text.substring(0, cut).trim();
        trimmed = trimmed.replaceAll("[\\s,;:.\\-]+$", "");
        trimmed = trimmed.replaceAll("(?i)\\s+(and|or|the|a|an|with|for|to|of|in|on|at|by)$", "").trim();
        return trimmed;
    }

    /** The first complete sentence, or the whole text when it has no sentence break. */
    public static String leadSentence(String value) {
        String text = clean(value);
        int end = -1;
        for (String ending : SENTENCE_ENDINGS) {
            int index = text.indexOf(ending);
            if (index >= 0 && (end < 0 || index < end)) {
                end = index;
            }
        }
        return end < 0 ? text : text.substring(0, end + 1).trim();
    }

    /** Lowercase, hyphenated slug fragment used for readable URL paths. */
    public static String slug(String value) {
        return clean(value).toLowerCase(Locale.ROOT)
                .replace("&", " and ")
                .replaceAll("\\([^)]*\\)", " ")
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");
    }
}
