package com.infeara.site.content;

public record NewsletterEdition(
        String slug,
        String editionLabel,
        String title,
        String excerpt,
        String content,
        String publishedAt,
        int readingMinutes
) { }
