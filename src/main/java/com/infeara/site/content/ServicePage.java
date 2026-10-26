package com.infeara.site.content;

import java.util.List;

public record ServicePage(
        String slug,
        String name,
        String category,
        String positioning,
        String summary,
        List<String> capabilities
) {
    public List<ServiceImage> images() {
        // Three images per service page, generated for this page only by
        // scripts/generate-page-images.py, so a service photograph never doubles as another page's.
        return List.of(
                new ServiceImage("pages/service-" + slug + "-1.webp", name + " service environment", capabilities.get(0)),
                new ServiceImage("pages/service-" + slug + "-2.webp", name + " service in practice", capabilities.get(1)),
                new ServiceImage("pages/service-" + slug + "-3.webp", name + " supporting technology environment", capabilities.get(2))
        );
    }

    public List<ServiceImage> getImages() { return images(); }
    public ServiceImage getHeroImage() { return images().get(0); }
    public List<ServiceImage> getGalleryImages() { return images().subList(1, images().size()); }
}
