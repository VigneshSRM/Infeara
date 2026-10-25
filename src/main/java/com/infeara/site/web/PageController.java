package com.infeara.site.web;

import java.time.Year;
import com.infeara.site.api.ConsultationRequest;
import com.infeara.site.api.ConsultationService;
import com.infeara.site.content.InsightService;
import com.infeara.site.content.NewsletterService;
import com.infeara.site.content.ServicePage;
import com.infeara.site.content.SpecialistServicePage;
import com.infeara.site.content.WebsiteContent;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.client.RestClientException;

@Controller
public class PageController {
    private final WebsiteContent content;
    private final InsightService insightService;
    private final NewsletterService newsletterService;
    private final ConsultationService consultationService;
    private final String canonicalBaseUrl;

    public PageController(WebsiteContent content, InsightService insightService,
                          NewsletterService newsletterService, ConsultationService consultationService,
                          @Value("${SITE_BASE_URL:http://localhost:8080}") String canonicalBaseUrl) {
        this.content = content;
        this.insightService = insightService;
        this.newsletterService = newsletterService;
        this.consultationService = consultationService;
        this.canonicalBaseUrl = canonicalBaseUrl.replaceAll("/+$", "");
    }

    @ModelAttribute
    public void addSharedPageData(Model model, HttpServletRequest request) {
        String requestPath = request.getRequestURI();
        if (requestPath == null || requestPath.isBlank()) requestPath = "/";
        if (!requestPath.startsWith("/")) requestPath = "/" + requestPath;
        model.addAttribute("canonicalUrl", canonicalBaseUrl + requestPath);
        model.addAttribute("serviceNavigation", content.services());
        model.addAttribute("serviceNavigationGroups", content.services().stream()
                .map(service -> java.util.Map.<String, Object>of(
                        "service", service,
                        "pages", content.specialistServices(service.slug())))
                .toList());
        model.addAttribute("allSpecialistServices", content.allSpecialistServices());
        model.addAttribute("industryNavigation", content.industries());
        model.addAttribute("currentYear", Year.now().getValue());
        model.addAttribute("activePage", activePage(request.getRequestURI()));
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("pageTitle", "Infeara Technologies | A Technology Partner for What’s Next");
        model.addAttribute("pageDescription", "A collaborative technology partner for IT infrastructure, cybersecurity, cloud, automation, and software.");
        model.addAttribute("services", content.featuredServices());
        model.addAttribute("technologyLogos", technologyLogos());
        model.addAttribute("latestInsights", insightService.findPublished().stream().limit(3).toList());
        return "home";
    }

    @GetMapping("/services")
    public String services(Model model) {
        model.addAttribute("pageTitle", "Technology Services | Infeara Technologies");
        model.addAttribute("pageDescription", "Explore Infeara Technologies services across cybersecurity, infrastructure, managed IT, cloud, networks, and software.");
        model.addAttribute("services", content.services());
        return "services";
    }

    @GetMapping("/services/{parentSlug}/{slug}/")
    public String canonicalSpecialistServicePath(@PathVariable String parentSlug, @PathVariable String slug) {
        return "redirect:/services/" + parentSlug + "/" + slug;
    }

    @GetMapping("/services/{parentSlug}/{slug}")
    public String specialistService(@PathVariable String parentSlug, @PathVariable String slug, Model model) {
        ServicePage parent = content.service(parentSlug).orElseThrow(() -> notFound("Service"));
        SpecialistServicePage service = content.specialistService(parentSlug, slug)
                .orElseThrow(() -> notFound("Service"));
        model.addAttribute("specialistService", service);
        model.addAttribute("parentService", parent);
        model.addAttribute("relatedSpecialistServices", content.specialistServices(parentSlug).stream()
                .filter(item -> !item.slug().equals(slug)).limit(3).toList());
        model.addAttribute("pageTitle", service.name() + " | " + parent.name() + " | Infeara Technologies");
        model.addAttribute("pageDescription", service.positioning());
        return "specialist-service-detail";
    }

    @GetMapping("/services/{slug}/")
    public String canonicalServicePath(@PathVariable String slug) {
        return "redirect:/services/" + slug;
    }

    @GetMapping("/services/{slug}")
    public String service(@PathVariable String slug, Model model) {
        ServicePage service = content.service(slug).orElseThrow(() -> notFound("Service"));
        model.addAttribute("service", service);
        model.addAttribute("specialistServices", content.specialistServices(slug));
        model.addAttribute("relatedServices", content.services().stream()
                .filter(item -> !item.slug().equals(slug)).limit(3).toList());
        model.addAttribute("pageTitle", service.name() + " | Infeara Technologies");
        model.addAttribute("pageDescription", service.positioning());
        return "service-detail";
    }

    @GetMapping("/industries/{slug}/")
    public String canonicalIndustryPath(@PathVariable String slug) {
        return "redirect:/industries/" + slug;
    }

    @GetMapping("/industries/{slug}")
    public String industry(@PathVariable String slug, Model model) {
        var industry = content.industries().stream()
                .filter(item -> item.slug().equals(slug))
                .findFirst()
                .orElseThrow(() -> notFound("Industry"));
        model.addAttribute("industry", industry);
        model.addAttribute("industryNumber", content.industries().indexOf(industry) + 1);
        model.addAttribute("industryImage", industry.imageFileName());
        model.addAttribute("industryImageAlt", industry.imageAlt());
        model.addAttribute("considerations", industry.considerations());
        model.addAttribute("relatedServices", industry.relatedServiceSlugs().stream()
                .map(serviceSlug -> content.service(serviceSlug).orElseThrow()).toList());
        model.addAttribute("pageTitle", industry.title() + " | Industry context | Infeara Technologies");
        model.addAttribute("pageDescription", industry.summary());
        return "industry-detail";
    }

    @GetMapping("/industries")
    public String industries(Model model) {
        model.addAttribute("pageTitle", "Industries & Work Environments | Infeara Technologies");
        model.addAttribute("pageDescription", "Explore business environments and technology challenges Infeara can help address.");
        model.addAttribute("industries", content.industries());
        return "industries";
    }

    @GetMapping("/careers")
    public String careers(Model model) {
        model.addAttribute("pageTitle", "Careers at Infeara | Infeara Technologies");
        model.addAttribute("pageDescription", "Learn about working at Infeara Technologies and explore current career opportunities.");
        return "careers";
    }

    @GetMapping("/careers/open-positions")
    public String careerOpenPositions(Model model) {
        model.addAttribute("pageTitle", "Open Positions | Careers at Infeara Technologies");
        model.addAttribute("pageDescription", "Browse current career openings at Infeara Technologies. Only verified positions are listed.");
        model.addAttribute("jobs", java.util.List.of());
        return "career-open-positions";
    }

    @GetMapping("/about")
    public String about(Model model) {
        model.addAttribute("pageTitle", "About Infeara Technologies");
        model.addAttribute("pageDescription", "A collaborative technology partner built around practical problem-solving and human partnership.");
        model.addAttribute("services", content.featuredServices());
        return "about";
    }

    @GetMapping("/clients")
    public String clients(Model model) {
        model.addAttribute("pageTitle", "Our Clients | Infeara Technologies");
        model.addAttribute("pageDescription", "Explore how Infeara Technologies approaches client collaboration and technology work.");
        model.addAttribute("clientLogos", java.util.List.of());
        model.addAttribute("clientTestimonials", java.util.List.of());
        return "clients";
    }

    @GetMapping("/partners")
    public String legacyPartners() {
        return "redirect:/clients";
    }

    private java.util.List<java.util.Map<String, String>> technologyLogos() {
        return java.util.List.of(
                java.util.Map.of("name", "Microsoft", "logoPath", "/assets/technologies/microsoft.svg"),
                java.util.Map.of("name", "Google", "logoPath", "/assets/technologies/google.svg"),
                java.util.Map.of("name", "Zoho", "logoPath", "/assets/technologies/zoho.svg"),
                java.util.Map.of("name", "Sophos", "logoPath", "/assets/technologies/sophos.png"),
                java.util.Map.of("name", "FortiGate", "logoPath", "/assets/technologies/fortinet.svg"),
                java.util.Map.of("name", "SonicWall", "logoPath", "/assets/technologies/sonicwall.svg"),
                java.util.Map.of("name", "NETGEAR", "logoPath", "/assets/technologies/netgear.svg"),
                java.util.Map.of("name", "Dell", "logoPath", "/assets/technologies/dell.svg"),
                java.util.Map.of("name", "Lenovo", "logoPath", "/assets/technologies/lenovo.svg"),
                java.util.Map.of("name", "Acer", "logoPath", "/assets/technologies/acer.svg"),
                java.util.Map.of("name", "HP", "logoPath", "/assets/technologies/hp.svg")
        );
    }

    @GetMapping("/newsletters/")
    public String canonicalNewslettersPath() {
        return "redirect:/newsletters";
    }

    @GetMapping("/newsletters")
    public String newsletters(Model model) {
        model.addAttribute("pageTitle", "Newsletters | Infeara Technologies");
        model.addAttribute("pageDescription", "Browse published Infeara Technologies newsletter editions.");
        model.addAttribute("newsletterEditions", newsletterService.findPublished());
        return "newsletters";
    }

    @GetMapping("/newsletters/{slug}/")
    public String canonicalNewsletterEditionPath(@PathVariable String slug) {
        return "redirect:/newsletters/" + slug;
    }

    @GetMapping("/newsletters/{slug}")
    public String newsletterEdition(@PathVariable String slug, Model model) {
        var edition = newsletterService.findPublished(slug).orElseThrow(() -> notFound("Newsletter edition"));
        model.addAttribute("edition", edition);
        model.addAttribute("pageTitle", edition.title() + " | Infeara Newsletters");
        model.addAttribute("pageDescription", edition.excerpt());
        return "newsletter-detail";
    }

    @GetMapping("/blogs/")
    public String canonicalBlogsPath() {
        return "redirect:/blogs";
    }

    @GetMapping("/blogs")
    public String blogs(@RequestParam(required = false) String q,
                        @RequestParam(required = false) String category, Model model) {
        var published = insightService.findPublished();
        var filtered = published.stream()
                .filter(article -> q == null || q.isBlank()
                        || (article.title() + " " + article.excerpt() + " " + article.content())
                                .toLowerCase(java.util.Locale.ROOT).contains(q.trim().toLowerCase(java.util.Locale.ROOT)))
                .filter(article -> category == null || category.isBlank() || article.category().equalsIgnoreCase(category))
                .toList();
        model.addAttribute("pageTitle", "Blogs | Infeara Technologies");
        model.addAttribute("pageDescription", "Practical blog articles on infrastructure, cybersecurity, cloud, automation, and business technology.");
        model.addAttribute("blogs", filtered);
        model.addAttribute("query", q == null ? "" : q);
        model.addAttribute("selectedCategory", category == null ? "" : category);
        model.addAttribute("categories", published.stream().map(article -> article.category()).distinct().sorted().toList());
        return "blogs";
    }

    @GetMapping("/blogs/{slug}/")
    public String canonicalBlogPath(@PathVariable String slug) {
        return "redirect:/blogs/" + slug;
    }

    @GetMapping("/blogs/{slug}")
    public String blog(@PathVariable String slug, Model model) {
        var blog = insightService.findPublished(slug).orElseThrow(() -> notFound("Blog article"));
        model.addAttribute("blog", blog);
        model.addAttribute("relatedBlogs", insightService.findPublished().stream()
                .filter(item -> !item.slug().equals(slug)).limit(2).toList());
        model.addAttribute("pageTitle", blog.title() + " | Infeara Blog");
        model.addAttribute("pageDescription", blog.excerpt());
        return "blog-detail";
    }

    @GetMapping("/insights")
    public String insights(@RequestParam(required = false) String q,
                           @RequestParam(required = false) String category, Model model) {
        var published = insightService.findPublished();
        var filtered = published.stream()
                .filter(article -> q == null || q.isBlank()
                        || (article.title() + " " + article.excerpt() + " " + article.content())
                                .toLowerCase(java.util.Locale.ROOT).contains(q.trim().toLowerCase(java.util.Locale.ROOT)))
                .filter(article -> category == null || category.isBlank() || article.category().equalsIgnoreCase(category))
                .toList();
        model.addAttribute("pageTitle", "Insights | Infeara Technologies");
        model.addAttribute("pageDescription", "Practical notes on infrastructure, cybersecurity, cloud, automation, and business technology.");
        model.addAttribute("insights", filtered);
        model.addAttribute("query", q == null ? "" : q);
        model.addAttribute("selectedCategory", category == null ? "" : category);
        model.addAttribute("categories", published.stream().map(article -> article.category()).distinct().sorted().toList());
        return "insights";
    }

    @GetMapping("/insights/{slug}")
    public String insight(@PathVariable String slug, Model model) {
        var article = insightService.findPublished(slug).orElseThrow(() -> notFound("Article"));
        model.addAttribute("article", article);
        model.addAttribute("relatedInsights", insightService.findPublished().stream()
                .filter(item -> !item.slug().equals(slug)).limit(2).toList());
        model.addAttribute("pageTitle", article.title() + " | Infeara Field Notes");
        model.addAttribute("pageDescription", article.excerpt());
        return "insight-detail";
    }

    @GetMapping("/contact")
    public String contact(@RequestParam(defaultValue = "false") boolean sent,
                          @RequestParam(required = false) String service, Model model) {
        model.addAttribute("pageTitle", "Contact Infeara Technologies");
        model.addAttribute("pageDescription", "Tell Infeara Technologies what you are working toward and request a consultation.");
        model.addAttribute("request", emptyRequest(service));
        model.addAttribute("sent", sent);
        return "contact";
    }

    @PostMapping("/contact")
    public String submitContact(@Valid @ModelAttribute("request") ConsultationRequest request,
                                BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("pageTitle", "Contact Infeara Technologies");
            model.addAttribute("pageDescription", "Tell Infeara Technologies what you are working toward and request a consultation.");
            model.addAttribute("formError", "Please check the required fields and try again.");
            return "contact";
        }
        if (request.website() != null && !request.website().isBlank()) {
            return "redirect:/contact?sent=true";
        }
        try {
            consultationService.save(request);
            redirectAttributes.addAttribute("sent", true);
            return "redirect:/contact";
        } catch (IllegalStateException | RestClientException exception) {
            model.addAttribute("pageTitle", "Contact Infeara Technologies");
            model.addAttribute("pageDescription", "Tell Infeara Technologies what you are working toward and request a consultation.");
            model.addAttribute("formError", "We could not send your enquiry just now. Please try again shortly.");
            return "contact";
        }
    }

    @GetMapping("/privacy")
    public String privacy(Model model) {
        model.addAttribute("pageTitle", "Privacy Notice | Infeara Technologies");
        model.addAttribute("pageDescription", "How Infeara Technologies handles information submitted through the website.");
        return "privacy";
    }

    @GetMapping("/terms")
    public String terms(Model model) {
        model.addAttribute("pageTitle", "Terms of Use | Infeara Technologies");
        model.addAttribute("pageDescription", "Terms for using the Infeara Technologies website.");
        return "terms";
    }

    @GetMapping({"/index.html", "/services.html", "/privacy.html", "/terms.html", "/about.html",
            "/industries.html", "/careers.html", "/partners.html", "/clients.html", "/insights.html", "/blogs.html", "/newsletters.html", "/contact.html"})
    public String oldHtmlRoutes(HttpServletRequest request) {
        String path = request.getRequestURI();
        if (path.endsWith("/index.html")) return "redirect:/";
        if (path.endsWith("/partners.html") || path.endsWith("/clients.html")) return "redirect:/clients";
        return "redirect:" + path.replaceFirst("\\.html$", "");
    }

    @GetMapping("/insight-{slug}.html")
    public String oldInsightRoute(@PathVariable String slug) {
        return "redirect:/insights/" + slug;
    }

    @GetMapping("/blog-{slug}.html")
    public String oldBlogRoute(@PathVariable String slug) {
        return "redirect:/blogs/" + slug;
    }

    @GetMapping("/newsletter-{slug}.html")
    public String oldNewsletterRoute(@PathVariable String slug) {
        return "redirect:/newsletters/" + slug;
    }

    private ConsultationRequest emptyRequest(String selectedService) {
        return new ConsultationRequest("", "", "", "", selectedService == null ? "" : selectedService,
                "", "", "/contact", "", false);
    }

    private ResponseStatusException notFound(String type) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, type + " not found");
    }

    private String activePage(String path) {
        if (path.startsWith("/services")) return "services";
        if (path.startsWith("/industries")) return "industries";
        if (path.startsWith("/careers")) return "careers";
        if (path.startsWith("/about")) return "about";
        if (path.startsWith("/clients")) return "clients";
        if (path.startsWith("/blogs")) return "blogs";
        if (path.startsWith("/insights")) return "insights";
        if (path.startsWith("/newsletters")) return "newsletters";
        if (path.startsWith("/contact")) return "contact";
        return "home";
    }
}
