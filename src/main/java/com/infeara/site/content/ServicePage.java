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
        List<String> photos = switch (slug) {
            case "cybersecurity" -> List.of("cybersecurity-overview.jpg", "cybersecurity-detail.jpg", "managed-it-detail.jpg");
            case "infrastructure" -> List.of("infrastructure-overview.jpg", "infrastructure-detail.jpg", "cloud-datacenter-overview.jpg");
            case "managed-it" -> List.of("managed-it-overview.jpg", "managed-it-detail.jpg", "infrastructure-detail.jpg");
            case "it-outsourcing" -> List.of("it-outsourcing-overview.jpg", "it-outsourcing-detail.jpg", "managed-it-overview.jpg");
            case "cloud-datacenter" -> List.of("cloud-datacenter-overview.jpg", "cloud-datacenter-detail.jpg", "infrastructure-detail.jpg");
            case "network-engineering" -> List.of("infrastructure-detail.jpg", "managed-it-detail.jpg", "cybersecurity-detail.jpg");
            case "facility-management" -> List.of("infrastructure-overview.jpg", "managed-it-detail.jpg", "infrastructure-detail.jpg");
            case "turnkey-projects" -> List.of("infrastructure-detail.jpg", "it-outsourcing-detail.jpg", "cloud-datacenter-overview.jpg");
            case "automation-software" -> List.of("managed-it-overview.jpg", "cloud-datacenter-detail.jpg", "it-outsourcing-detail.jpg");
            default -> throw new IllegalStateException("No service photography configured for " + slug);
        };
        return List.of(
                new ServiceImage(photos.get(0), name + " service environment", capabilities.get(0)),
                new ServiceImage(photos.get(1), name + " service in practice", capabilities.get(1)),
                new ServiceImage(photos.get(2), name + " supporting technology environment", capabilities.get(2))
        );
    }

    public List<ServiceImage> getImages() { return images(); }
    public ServiceImage getHeroImage() { return images().get(0); }
    public List<ServiceImage> getGalleryImages() { return images().subList(1, images().size()); }
}
