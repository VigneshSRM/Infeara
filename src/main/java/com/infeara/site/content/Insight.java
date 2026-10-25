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
) { }
