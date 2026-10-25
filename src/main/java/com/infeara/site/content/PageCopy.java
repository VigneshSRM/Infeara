package com.infeara.site.content;

import java.util.List;

/**
 * Authored copy for one page: its search-facing metadata plus the sections rendered on the page.
 *
 * <p>Field meanings are shared across page types and labelled per template:</p>
 * <ul>
 *   <li>{@code covers} — service pages: what the work includes; industry pages: typical systems.</li>
 *   <li>{@code signs} — service pages: signals the work is worth reviewing; industry pages:
 *       where teams in that sector usually get stuck.</li>
 *   <li>{@code deliverables} — service pages: what the client receives; industry pages: how we
 *       support the sector.</li>
 * </ul>
 */
public record PageCopy(
        String seoTitle,
        String metaDescription,
        String intro,
        List<String> covers,
        List<String> signs,
        List<String> deliverables,
        List<Faq> faqs
) {
    /** A visible question and answer pair. No FAQPage markup is emitted for these. */
    public record Faq(String question, String answer) { }

    public boolean hasSections() {
        return !covers.isEmpty() || !signs.isEmpty() || !deliverables.isEmpty() || !faqs.isEmpty();
    }
}
