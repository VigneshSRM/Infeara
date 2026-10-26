package com.infeara.site.content;

import java.util.Arrays;
import java.util.List;

public record IndustryPage(String slug, String title, String summary, String challenge) {
    public List<String> considerations() {
        return Arrays.stream(challenge.split(",\\s*(?:and\\s+)?"))
                .map(String::trim)
                .map(item -> item.replaceFirst("^(?i:and)\\s+", ""))
                .filter(item -> !item.isBlank())
                .toList();
    }

    public List<String> relatedServiceSlugs() {
        return switch (slug) {
            case "healthcare-life-sciences" -> List.of("cybersecurity", "cloud-datacenter", "network-engineering");
            case "banking-financial-services" -> List.of("cybersecurity", "infrastructure", "cloud-datacenter");
            case "insurance" -> List.of("cybersecurity", "cloud-datacenter", "automation-software");
            case "retail-e-commerce" -> List.of("network-engineering", "cloud-datacenter", "automation-software");
            case "manufacturing-industrial" -> List.of("network-engineering", "infrastructure", "cybersecurity");
            case "real-estate-property-management" -> List.of("facility-management", "network-engineering", "managed-it");
            case "education-edtech" -> List.of("network-engineering", "managed-it", "cybersecurity");
            case "information-technology-saas" -> List.of("cloud-datacenter", "cybersecurity", "automation-software");
            case "logistics-supply-chain" -> List.of("network-engineering", "cloud-datacenter", "automation-software");
            case "hospitality-food-services" -> List.of("managed-it", "network-engineering", "cloud-datacenter");
            case "telecommunications" -> List.of("network-engineering", "infrastructure", "cybersecurity");
            case "government-public-sector" -> List.of("cybersecurity", "infrastructure", "it-outsourcing");
            case "automotive-transportation" -> List.of("network-engineering", "infrastructure", "cybersecurity");
            case "energy-utilities" -> List.of("network-engineering", "cybersecurity", "infrastructure");
            case "agriculture-agritech" -> List.of("network-engineering", "cloud-datacenter", "automation-software");
            case "media-entertainment" -> List.of("cloud-datacenter", "infrastructure", "automation-software");
            case "travel-tourism" -> List.of("cloud-datacenter", "network-engineering", "cybersecurity");
            case "construction-engineering" -> List.of("network-engineering", "managed-it", "cloud-datacenter");
            case "professional-services" -> List.of("cybersecurity", "managed-it", "it-outsourcing");
            case "pharmaceuticals-biotechnology" -> List.of("cybersecurity", "infrastructure", "cloud-datacenter");
            case "wholesale-distribution" -> List.of("automation-software", "cloud-datacenter", "network-engineering");
            case "fmcg" -> List.of("infrastructure", "managed-it", "network-engineering");
            case "legal-services" -> List.of("cybersecurity", "cloud-datacenter", "managed-it");
            case "non-profit-organizations" -> List.of("managed-it", "it-outsourcing", "cybersecurity");
            default -> List.of("managed-it", "infrastructure", "cybersecurity");
        };
    }

    /**
     * Sector photograph for this industry, framed for this page only. scripts/generate-page-images.py
     * derives one file per industry from the studio photography, so no two industry pages show the
     * same image.
     */
    public String imageFileName() {
        return "pages/" + slug + ".webp";
    }

    /** Base photograph the industry page is cropped from, used to keep the alt text accurate. */
    private String baseFileName() {
        return switch (slug) {
            case "healthcare-life-sciences", "pharmaceuticals-biotechnology" -> "healthcare-life-sciences.jpg";
            case "banking-financial-services", "insurance", "professional-services", "legal-services" -> "finance-professional-services.jpg";
            case "retail-e-commerce", "wholesale-distribution", "fmcg" -> "retail-distribution.jpg";
            case "manufacturing-industrial", "construction-engineering" -> "manufacturing-engineering.jpg";
            case "real-estate-property-management", "hospitality-food-services", "travel-tourism" -> "property-hospitality-travel.jpg";
            case "education-edtech" -> "education-edtech.jpg";
            case "information-technology-saas", "media-entertainment", "telecommunications" -> "digital-products-media-telecom.jpg";
            case "logistics-supply-chain", "automotive-transportation" -> "logistics-transportation.jpg";
            case "government-public-sector", "non-profit-organizations" -> "public-service-nonprofit.jpg";
            case "energy-utilities", "agriculture-agritech" -> "energy-utilities.jpg";
            default -> "digital-products-media-telecom.jpg";
        };
    }

    public String imageAlt() {
        return switch (baseFileName()) {
            case "healthcare-life-sciences.jpg" -> "Research scientist working in a modern laboratory.";
            case "finance-professional-services.jpg" -> "Professionals reviewing information together in a modern office.";
            case "retail-distribution.jpg" -> "Operations staff scanning a parcel in an organized fulfillment space.";
            case "manufacturing-engineering.jpg" -> "Industrial engineer inspecting equipment on a production floor.";
            case "property-hospitality-travel.jpg" -> "Hospitality staff member checking a tablet in a contemporary property interior.";
            case "education-edtech.jpg" -> "Educator and learners collaborating in a modern campus space.";
            case "digital-products-media-telecom.jpg" -> "Digital product team working with data displays in a collaborative studio.";
            case "logistics-transportation.jpg" -> "Logistics supervisor reviewing operations inside a distribution center.";
            case "public-service-nonprofit.jpg" -> "Civic service colleagues collaborating in a shared workspace.";
            case "energy-utilities.jpg" -> "Operations engineer monitoring infrastructure on digital displays.";
            default -> "Technology operations in a professional environment.";
        };
    }
}
