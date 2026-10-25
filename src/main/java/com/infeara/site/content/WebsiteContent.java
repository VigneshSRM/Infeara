package com.infeara.site.content;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

@Component
public class WebsiteContent {
    private final List<ServicePage> services = List.of(
            new ServicePage("cybersecurity", "Cybersecurity services", "PROTECT / CYBERSECURITY",
                    "Identify risks. Strengthen defenses. Protect business operations.",
                    "Understand your security posture and prioritize practical improvements with clear scope and responsible assessment.",
                    List.of("Vulnerability assessment & penetration testing (VAPT)", "Network, application, and cloud security audits", "Endpoint, firewall, and perimeter security audits", "Identity and access management review", "Red team and wireless network assessments", "Threat intelligence and disaster recovery audits")),
            new ServicePage("infrastructure", "IT infrastructure services", "BUILD / INFRASTRUCTURE",
                    "Build and maintain the technology foundation your business depends on.",
                    "Plan, implement, and care for the servers, storage, virtualization, and systems that keep work moving.",
                    List.of("Infrastructure assessment and modernization", "Server implementation, administration, and maintenance", "Storage and virtualization management", "Infrastructure monitoring and capacity planning", "Backup and recovery planning", "Infrastructure documentation")),
            new ServicePage("managed-it", "Managed IT services", "OPERATE / MANAGED IT",
                    "Reliable IT operations without unnecessary operational complexity.",
                    "Support day-to-day technology operations with monitoring, preventive maintenance, and clear ownership.",
                    List.of("Server and system administration", "Operations monitoring", "Incident and problem management", "Preventive maintenance and patch coordination", "Backup monitoring and recovery coordination", "Asset management and operational reporting")),
            new ServicePage("it-outsourcing", "IT outsourcing & remote support", "EXTEND / IT OUTSOURCING",
                    "Extend your IT capabilities with flexible technical support.",
                    "Add support capacity for infrastructure, system administration, application maintenance, and technical escalations.",
                    List.of("Remote IT support", "Infrastructure administration outsourcing", "System administration support", "IT resource augmentation", "Application maintenance and support", "Technical escalation support")),
            new ServicePage("cloud-datacenter", "Cloud & datacenter services", "MODERNIZE / CLOUD",
                    "Plan, secure, and operate cloud and datacenter environments.",
                    "Make informed choices across cloud, hybrid, and on-premises environments, with scope matched to your needs.",
                    List.of("AWS, Microsoft Azure, and Google Cloud infrastructure services", "Hybrid cloud and on-premises management", "Cloud and datacenter audits", "Cloud migration planning", "Cloud cost optimization", "Cloud backup and recovery")),
            new ServicePage("network-engineering", "Network engineering", "CONNECT / NETWORKS",
                    "Connect people, systems, and locations reliably.",
                    "Design, implement, and improve network foundations that support secure communication and daily operations.",
                    List.of("LAN and WAN implementation", "Enterprise network design", "Routing, switching, VLANs, and segmentation", "Wi-Fi infrastructure", "Firewall deployment and VPN connectivity", "Network monitoring, troubleshooting, and documentation")),
            new ServicePage("facility-management", "IT facility management", "MAINTAIN / FACILITIES",
                    "Keep IT equipment and infrastructure organized and operational.",
                    "Bring structure to physical IT environments, equipment lifecycle, and maintenance coordination.",
                    List.of("Onsite infrastructure support", "Server room operations", "Asset inventory and lifecycle tracking", "Equipment installation and relocation", "Physical infrastructure inspection", "Preventive maintenance and health reporting")),
            new ServicePage("turnkey-projects", "Turnkey IT infrastructure projects", "DELIVER / PROJECTS",
                    "From infrastructure planning to deployment and handover.",
                    "Coordinate a project from requirements discovery through implementation, validation, and knowledge transfer.",
                    List.of("Requirements discovery and site assessment", "Capacity planning and architecture design", "Bill of materials and cost estimation", "Procurement and vendor coordination", "Server, storage, and network deployment", "Testing, documentation, and handover")),
            new ServicePage("automation-software", "Automation & custom software", "IMPROVE / AUTOMATION",
                    "Turn repetitive work and business requirements into practical digital solutions.",
                    "Identify useful opportunities to streamline work, integrate applications, and build software around real needs.",
                    List.of("IT infrastructure automation", "Patch and maintenance workflows", "Monitoring and alerting automation", "AI-assisted operations with human oversight", "Custom business application development", "Workflow digitization, integration, and support"))
    );

    private final List<SpecialistServicePage> cybersecurityServices = List.of(
            new SpecialistServicePage("cybersecurity", "vulnerability-assessment-penetration-testing",
                    "Vulnerability assessment & penetration testing (VAPT)", "CYBERSECURITY / VAPT",
                    "Find and validate security weaknesses within an agreed, authorized scope.",
                    "Review exposed systems, identify and safely validate vulnerabilities, and provide a prioritized report for remediation. Testing starts only after written authorization and agreed rules of engagement.",
                    List.of("Scope definition and asset confirmation", "Vulnerability discovery and controlled validation", "Risk-ranked findings and remediation guidance", "Retest planning where agreed"),
                    List.of(new ServiceImage("cybersecurity-overview.jpg", "Security analyst reviewing a threat-monitoring workspace", "Security posture review"),
                            new ServiceImage("cybersecurity-detail.jpg", "Security specialist checking a protected network appliance", "Authorized validation"),
                            new ServiceImage("managed-it-detail.jpg", "IT operations specialist performing a scheduled system check", "Operations context"))),
            new SpecialistServicePage("cybersecurity", "network-application-cloud-security-audits",
                    "Network, application, and cloud security audits", "CYBERSECURITY / SECURITY AUDITS",
                    "Review network, application, and cloud controls against your environment and requirements.",
                    "Assess selected network, application, and cloud configurations, access controls, and operating practices. Findings are documented with context and practical priorities; audit scope is confirmed before work begins.",
                    List.of("Network architecture and security-control review", "Application configuration and exposure review", "Cloud identity, network, and storage configuration review", "Findings register and prioritized recommendations"),
                    List.of(new ServiceImage("cloud-datacenter-overview.jpg", "Enterprise cloud and datacenter environment", "Cloud and infrastructure review"),
                            new ServiceImage("cybersecurity-detail.jpg", "Security specialist reviewing a protected network environment", "Control assessment"),
                            new ServiceImage("infrastructure-detail.jpg", "Infrastructure engineer inspecting network equipment", "Network environment"))),
            new SpecialistServicePage("cybersecurity", "endpoint-firewall-perimeter-audits",
                    "Endpoint, firewall, and perimeter security audits", "CYBERSECURITY / ENDPOINT & PERIMETER",
                    "Understand how endpoint and perimeter controls are configured and monitored.",
                    "Review selected endpoint protections, firewall rules, perimeter exposure, and related monitoring practices. The output highlights observed gaps and gives your team a clear basis for prioritizing changes.",
                    List.of("Endpoint protection and policy review", "Firewall rule and configuration review", "External exposure and perimeter control review", "Monitoring, exception, and remediation recommendations"),
                    List.of(new ServiceImage("cybersecurity-detail.jpg", "Security specialist checking a protected network appliance", "Firewall and perimeter review"),
                            new ServiceImage("managed-it-detail.jpg", "IT operations specialist performing a scheduled system check", "Endpoint operations"),
                            new ServiceImage("cybersecurity-overview.jpg", "Security operations analyst reviewing security alerts", "Security monitoring"))),
            new SpecialistServicePage("cybersecurity", "identity-access-management-review",
                    "Identity and access management review", "CYBERSECURITY / IDENTITY & ACCESS",
                    "Review how identities, permissions, and privileged access are managed.",
                    "Examine selected identity and access practices, including account lifecycle, authentication, role assignments, and privileged access. Recommendations are shaped around your current systems and approved scope.",
                    List.of("Identity lifecycle and account governance review", "Role, permission, and privileged-access review", "Authentication and access-policy review", "Practical access-governance recommendations"),
                    List.of(new ServiceImage("cybersecurity-overview.jpg", "Security analyst reviewing identity and threat-monitoring controls", "Identity control review"),
                            new ServiceImage("it-outsourcing-overview.jpg", "IT support professional using a secure workstation", "Access operations"),
                            new ServiceImage("cybersecurity-detail.jpg", "Security specialist reviewing protected access controls", "Security review"))),
            new SpecialistServicePage("cybersecurity", "red-team-wireless-assessments",
                    "Red team and wireless network assessments", "CYBERSECURITY / RED TEAM & WIRELESS",
                    "Assess selected attack paths and wireless controls within strict written boundaries.",
                    "Plan an authorized red-team or wireless assessment around explicit assets, methods, safety limits, and rules of engagement. Findings are reported responsibly, with sensitive data handled under the agreed process.",
                    List.of("Written scope, authorization, and rules of engagement", "Controlled assessment of agreed attack paths", "Wireless configuration and exposure review", "Evidence handling, reporting, and debrief"),
                    List.of(new ServiceImage("cybersecurity-overview.jpg", "Security operations analyst assessing potential attack paths", "Authorized red-team planning"),
                            new ServiceImage("infrastructure-detail.jpg", "Infrastructure engineer inspecting network equipment", "Wireless and network review"),
                            new ServiceImage("cybersecurity-detail.jpg", "Security specialist reviewing an authorized test environment", "Assessment findings"))),
            new SpecialistServicePage("cybersecurity", "threat-intelligence-disaster-recovery-audits",
                    "Threat intelligence and disaster recovery audits", "CYBERSECURITY / INTELLIGENCE & RECOVERY",
                    "Improve visibility into relevant threats and review readiness for disruption.",
                    "Review how threat information is assessed and used, alongside selected disaster-recovery plans, dependencies, and recovery procedures. The work identifies documentation and readiness gaps for your team to evaluate.",
                    List.of("Threat-information sources and relevance review", "Escalation and response-process review", "Recovery-plan, dependency, and ownership review", "Exercise, documentation, and improvement priorities"),
                    List.of(new ServiceImage("managed-it-overview.jpg", "IT operations analyst monitoring service health and events", "Threat monitoring context"),
                            new ServiceImage("infrastructure-overview.jpg", "Resilient enterprise server infrastructure", "Recovery environment"),
                            new ServiceImage("cloud-datacenter-detail.jpg", "Cloud infrastructure architect reviewing a recovery topology", "Continuity planning")))
    );

    private final List<IndustryPage> industries = List.of(
            new IndustryPage("healthcare-life-sciences", "Healthcare & Life Sciences",
                    "Technology environments for clinical, research, laboratory, and administrative work.",
                    "Availability, identity and access, data handling, backups, network separation, and integration needs."),
            new IndustryPage("banking-financial-services", "Banking & Financial Services",
                    "Technology supporting financial operations, customer channels, and internal teams.",
                    "Resilience, access controls, monitoring, audit trails, continuity planning, and system integrations."),
            new IndustryPage("insurance", "Insurance",
                    "Technology environments spanning policy administration, claims workflows, and customer services.",
                    "Workflow integration, secure access, document handling, operational reporting, and recovery readiness."),
            new IndustryPage("retail-e-commerce", "Retail & E-commerce",
                    "Connected systems for stores, digital storefronts, orders, and customer interactions.",
                    "Point-of-sale and payment integrations, inventory visibility, seasonal capacity, and service continuity."),
            new IndustryPage("manufacturing-industrial", "Manufacturing & Industrial",
                    "Technology used across business systems, production environments, and operational sites.",
                    "IT/OT boundaries, plant connectivity, system availability, maintenance coordination, and recovery planning."),
            new IndustryPage("real-estate-property-management", "Real Estate & Property Management",
                    "Platforms and infrastructure supporting property portfolios, building operations, and tenant services.",
                    "Building-system connectivity, access management, multi-site networks, asset records, and service continuity."),
            new IndustryPage("education-edtech", "Education & EdTech",
                    "Technology environments for learning platforms, campus services, and administrative teams.",
                    "Campus connectivity, identity lifecycle, endpoint management, platform availability, and data handling."),
            new IndustryPage("information-technology-saas", "Information Technology & SaaS",
                    "Cloud and software environments used to build, deliver, and support digital products.",
                    "Cloud foundations, identity, deployment workflows, monitoring, security practices, and capacity planning."),
            new IndustryPage("logistics-supply-chain", "Logistics & Supply Chain",
                    "Connected technology across warehouses, transport operations, suppliers, and customers.",
                    "Warehouse and fleet connectivity, partner integrations, tracking systems, data exchange, and resilience."),
            new IndustryPage("hospitality-food-services", "Hospitality & Food Services",
                    "Technology supporting guest experiences, bookings, service teams, and food operations.",
                    "Reservations and point-of-sale systems, payment security, guest networks, site connectivity, and backups."),
            new IndustryPage("telecommunications", "Telecommunications",
                    "Network and service platforms used to connect customers, sites, and operational teams.",
                    "Network visibility, capacity, service availability, access governance, and operational security."),
            new IndustryPage("government-public-sector", "Government & Public Sector",
                    "Technology environments supporting public services, internal operations, and records.",
                    "Service continuity, identity and access, information handling, system integration, and documented requirements."),
            new IndustryPage("automotive-transportation", "Automotive & Transportation",
                    "Technology spanning mobility services, vehicle operations, workshops, and transport networks.",
                    "Fleet systems, connected devices, site connectivity, operational data, and continuity planning."),
            new IndustryPage("energy-utilities", "Energy & Utilities",
                    "Digital systems used in energy, utility, field, and infrastructure operations.",
                    "Operational network boundaries, monitoring, privileged access, backup readiness, and service resilience."),
            new IndustryPage("agriculture-agritech", "Agriculture & AgriTech",
                    "Technology supporting agricultural operations, field teams, equipment, and connected systems.",
                    "Rural connectivity, sensor and device management, remote access, data integration, and local resilience."),
            new IndustryPage("media-entertainment", "Media & Entertainment",
                    "Technology environments for content production, collaboration, and distribution.",
                    "Large-file storage, creative workflows, access management, collaboration, and distribution capacity."),
            new IndustryPage("travel-tourism", "Travel & Tourism",
                    "Digital platforms and operational systems for travel services and visitor experiences.",
                    "Booking integrations, customer-facing availability, payment workflows, distributed sites, and recovery."),
            new IndustryPage("construction-engineering", "Construction & Engineering",
                    "Technology for project teams, engineering workflows, and distributed job sites.",
                    "Site connectivity, project platforms, design-file access, collaboration, device security, and backups."),
            new IndustryPage("professional-services", "Professional Services",
                    "Business technology for client work, knowledge sharing, and distributed teams.",
                    "Secure collaboration, identity, document workflows, remote access, continuity, and information handling."),
            new IndustryPage("pharmaceuticals-biotechnology", "Pharmaceuticals & Biotechnology",
                    "Technology environments for research, laboratory, production, and business operations.",
                    "Controlled access, data integrity, system availability, auditability needs, backups, and integration boundaries."),
            new IndustryPage("wholesale-distribution", "Wholesale & Distribution",
                    "Connected systems for inventory, order processing, warehouses, and supplier networks.",
                    "ERP and warehouse integration, inventory accuracy, partner connectivity, order workflows, and recovery."),
            new IndustryPage("fmcg", "FMCG",
                    "Technology supporting fast-moving consumer goods operations and supply networks.",
                    "Plant and office connectivity, supply-chain systems, endpoint management, visibility, and continuity."),
            new IndustryPage("legal-services", "Legal Services",
                    "Technology for matter workflows, document-heavy work, and client collaboration.",
                    "Secure access, document management, retention requirements, remote work, and backup readiness."),
            new IndustryPage("non-profit-organizations", "Non-Profit Organizations",
                    "Practical technology for mission delivery, fundraising, programs, and distributed teams.",
                    "Cost-aware operations, donor and program systems, identity, access, data handling, and recovery planning.")
    );

    public List<ServicePage> services() { return services; }
    public List<ServicePage> featuredServices() { return services.subList(0, 6); }
    public Optional<ServicePage> service(String slug) { return services.stream().filter(item -> item.slug().equals(slug)).findFirst(); }

    public List<SpecialistServicePage> specialistServices(String parentSlug) {
        if ("cybersecurity".equals(parentSlug)) return cybersecurityServices;
        return service(parentSlug).map(parent -> parent.capabilities().stream()
                .map(capability -> capabilityPage(parent, capability)).toList()).orElse(List.of());
    }

    public List<SpecialistServicePage> allSpecialistServices() {
        return services.stream().flatMap(parent -> specialistServices(parent.slug()).stream()).toList();
    }

    public Optional<SpecialistServicePage> specialistService(String parentSlug, String slug) {
        return specialistServices(parentSlug).stream().filter(item -> item.slug().equals(slug)).findFirst();
    }

    private SpecialistServicePage capabilityPage(ServicePage parent, String capability) {
        String name = capability.trim();
        String positioning = name + " shaped around your environment and agreed requirements.";
        String summary = "A focused " + name.toLowerCase(java.util.Locale.ROOT) + " workstream within "
                + parent.name().toLowerCase(java.util.Locale.ROOT)
                + ". We confirm requirements and dependencies first, then agree the scope, responsibilities, validation, and documentation or handover appropriate to the engagement.";
        List<String> scope = List.of(
                "Confirm the current environment, requirements, and dependencies",
                "Agree included work, boundaries, roles, and responsibilities",
                "Plan and complete the selected work with suitable validation",
                "Document decisions, findings, and handover steps"
        );
        List<String> photos = capabilityPhotos(parent.slug());
        int index = parent.capabilities().indexOf(capability);
        String alt = "Technology environment relevant to " + name.toLowerCase(java.util.Locale.ROOT);
        List<ServiceImage> images = List.of(
                new ServiceImage(parent.images().get(0).fileName(), alt, name),
                new ServiceImage(photos.get(index), alt, name),
                new ServiceImage(photos.get((index + 1) % photos.size()), alt, name)
        );
        return new SpecialistServicePage(parent.slug(), slugify(name), name,
                parent.category() + " / SERVICE SCOPE", positioning, summary, scope, images);
    }

    private List<String> capabilityPhotos(String parentSlug) {
        return switch (parentSlug) {
            case "infrastructure" -> List.of("infrastructure-overview.jpg", "infrastructure-detail.jpg",
                    "cloud-datacenter-overview.jpg", "managed-it-overview.jpg", "cloud-datacenter-detail.jpg", "managed-it-detail.jpg");
            case "managed-it" -> List.of("managed-it-overview.jpg", "managed-it-detail.jpg",
                    "infrastructure-overview.jpg", "infrastructure-detail.jpg", "it-outsourcing-overview.jpg", "it-outsourcing-detail.jpg");
            case "it-outsourcing" -> List.of("it-outsourcing-overview.jpg", "it-outsourcing-detail.jpg",
                    "managed-it-overview.jpg", "cybersecurity-overview.jpg", "cloud-datacenter-detail.jpg", "infrastructure-detail.jpg");
            case "cloud-datacenter" -> List.of("cloud-datacenter-overview.jpg", "cloud-datacenter-detail.jpg",
                    "infrastructure-overview.jpg", "infrastructure-detail.jpg", "managed-it-overview.jpg", "managed-it-detail.jpg");
            case "network-engineering" -> List.of("infrastructure-detail.jpg", "cybersecurity-detail.jpg",
                    "managed-it-detail.jpg", "infrastructure-overview.jpg", "it-outsourcing-detail.jpg", "cloud-datacenter-overview.jpg");
            case "facility-management" -> List.of("infrastructure-overview.jpg", "infrastructure-detail.jpg",
                    "managed-it-detail.jpg", "managed-it-overview.jpg", "it-outsourcing-detail.jpg", "cloud-datacenter-overview.jpg");
            case "turnkey-projects" -> List.of("infrastructure-detail.jpg", "infrastructure-overview.jpg",
                    "it-outsourcing-detail.jpg", "cloud-datacenter-overview.jpg", "managed-it-detail.jpg", "managed-it-overview.jpg");
            case "automation-software" -> List.of("managed-it-overview.jpg", "cloud-datacenter-detail.jpg",
                    "it-outsourcing-detail.jpg", "cybersecurity-overview.jpg", "infrastructure-detail.jpg", "managed-it-detail.jpg");
            default -> throw new IllegalArgumentException("No capability photography configured for " + parentSlug);
        };
    }

    private String slugify(String value) {
        return value.toLowerCase(java.util.Locale.ROOT)
                .replace("&", " and ")
                .replaceAll("\\([^)]*\\)", " ")
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");
    }

    public List<SpecialistServicePage> cybersecurityServices() { return cybersecurityServices; }
    public List<IndustryPage> industries() { return industries; }
}
