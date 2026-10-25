package com.infeara.site.content;

import java.util.List;

public record SpecialistServicePage(
        String parentSlug,
        String slug,
        String name,
        String category,
        String positioning,
        String summary,
        List<String> capabilities,
        List<ServiceImage> images
) {
    public ServiceImage getHeroImage() { return images.get(0); }
    public List<ServiceImage> getGalleryImages() { return images.subList(1, images.size()); }
}
