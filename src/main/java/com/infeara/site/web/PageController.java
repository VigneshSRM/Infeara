package com.infeara.site.web;

import java.time.Year;
import java.util.List;
import java.util.Map;

import com.infeara.site.api.ConsultationRequest;
import com.infeara.site.api.ConsultationService;
import com.infeara.site.content.Insight;
import com.infeara.site.content.InsightService;
import com.infeara.site.content.IndustryPage;
import com.infeara.site.content.NewsletterEdition;
import com.infeara.site.content.NewsletterService;
import com.infeara.site.content.ServicePage;
import com.infeara.site.content.SpecialistServicePage;
import com.infeara.site.content.WebsiteContent;
import com.infeara.site.seo.SeoProperties;
import com.infeara.site.seo.SeoService;
import com.infeara.site.seo.SeoText;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
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
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.client.RestClientException;

/**
 * Renders every public page with its own title, description, canonical URL, robots directive,
 * Open Graph fields and JSON-LD, and answers legacy URLs with one-hop permanent redirects.
 */
@Controller
public class PageController {
    private final WebsiteContent content;
    private final InsightService insightService;
    private final NewsletterService newsletterService;
    private final ConsultationService consultationService;
    private final SeoService seo;

    public PageController(WebsiteContent content, InsightService insightService,
                          NewsletterService newsletterService, ConsultationService consultationService,
                          SeoService seo) {
        this.content = content;
        this.insightService = insightService;
        this.newsletterService = newsletterService;
        this.consultationService = consultationService;
        this.seo = seo;
    }

    /**
     * SEO defaults for every page rendered by this controller. Page handlers override the values
     * that are specific to them, so the shared head fragment can always read these attributes.
     */
    @ModelAttribute
    public void addSharedPageData(Model model, HttpServletRequest request) {
        model.addAttribute("canonicalUrl", seo.canonical(requestPath(request)));
        model.addAttribute("robotsMeta", seo.robotsDirective());
        model.addAttribute("ogType", "website");
        model.addAttribute("ogImage", seo.socialImage());
        model.addAttribute("ogArticlePublishedTime", "");
        model.addAttribute("siteName", SeoProperties.SITE_NAME);
        model.addAttribute("jsonLd", "");
        model.addAttribute("preloadImage", "");
        model.addAttribute("serviceNavigation", content.services());
        model.addAttribute("serviceNavigationGroups", content.services().stream()
                .map(service -> Map.<String, Object>of(
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
        model.addAttribute("pageDescription", SeoText.description(
                "A collaborative technology partner for IT infrastructure, cybersecurity, cloud, automation, and custom software, built on practical, clearly scoped delivery."));
        model.addAttribute("jsonLd", seo.homeGraph());
        model.addAttribute("services", content.featuredServices());
        model.addAttribute("technologyLogos", technologyLogos());
        model.addAttribute("latestInsights", insightService.findPublished().stream().limit(3).toList());
        return "home";
    }

    @GetMapping("/services")
    public String services(Model model) {
        model.addAttribute("pageTitle", "Technology Services | Infeara Technologies");
        model.addAttribute("pageDescription", SeoText.description(
                "Explore Infeara Technologies services across cybersecurity, infrastructure, managed IT, cloud, networks, and software, each with a scope agreed with your team."));
        model.addAttribute("jsonLd", seo.breadcrumbGraph(List.of(
                seo.crumb("Home", "/"),
                seo.crumb("Services", "/services"))));
        model.addAttribute("services", content.services());
        return "services";
    }

    @GetMapping("/services/{slug}")
    public String service(@PathVariable String slug, Model model) {
        ServicePage service = content.service(slug).orElseThrow(() -> notFound("Service"));
        String canonical = seo.canonical("/services/" + slug);
        model.addAttribute("service", service);
        model.addAttribute("specialistServices", content.specialistServices(slug));
        model.addAttribute("relatedServices", content.services().stream()
                .filter(item -> !item.slug().equals(slug)).limit(3).toList());
        model.addAttribute("pageTitle", SeoText.title(service.name()));
        model.addAttribute("pageDescription", SeoText.description(service.positioning(), service.summary()));
        model.addAttribute("jsonLd", seo.serviceGraph(
                List.of(seo.crumb("Home", "/"), seo.crumb("Services", "/services"),
                        seo.crumb(service.name(), null)),
                service.name(), service.summary(), canonical));
        model.addAttribute("preloadImage", "/assets/services/" + service.getHeroImage().fileName());
        return "service-detail";
    }

    @GetMapping("/services/{parentSlug}/{slug}")
    public String specialistService(@PathVariable String parentSlug, @PathVariable String slug, Model model) {
        ServicePage parent = content.service(parentSlug).orElseThrow(() -> notFound("Service"));
        SpecialistServicePage service = content.specialistService(parentSlug, slug)
                .orElseThrow(() -> notFound("Service"));
        String canonical = seo.canonical("/services/" + parentSlug + "/" + slug);
        model.addAttribute("specialistService", service);
        model.addAttribute("parentService", parent);
        model.addAttribute("relatedSpecialistServices", content.specialistServices(parentSlug).stream()
                .filter(item -> !item.slug().equals(slug)).limit(3).toList());
        model.addAttribute("pageTitle", SeoText.title(service.name()));
        model.addAttribute("pageDescription", SeoText.description(service.positioning(), service.summary()));
        model.addAttribute("jsonLd", seo.serviceGraph(
                List.of(seo.crumb("Home", "/"), seo.crumb("Services", "/services"),
                        seo.crumb(parent.name(), "/services/" + parentSlug),
                        seo.crumb(service.name(), null)),
                service.name(), service.summary(), canonical));
        model.addAttribute("preloadImage", "/assets/services/" + service.getHeroImage().fileName());
        return "specialist-service-detail";
    }

    @GetMapping("/industries")
    public String industries(Model model) {
        model.addAttribute("pageTitle", "Industries & Work Environments | Infeara Technologies");
        model.addAttribute("pageDescription", SeoText.description(
                "Explore business environments and technology challenges Infeara can help address, as starting points for a conversation about your context and requirements."));
        model.addAttribute("jsonLd", seo.breadcrumbGraph(List.of(
                seo.crumb("Home", "/"),
                seo.crumb("Industries", "/industries"))));
        model.addAttribute("industries", content.industries());
        return "industries";
    }

    @GetMapping("/industries/{slug}")
    public String industry(@PathVariable String slug, Model model) {
        IndustryPage industry = content.industries().stream()
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
        model.addAttribute("pageTitle", SeoText.title(industry.title()));
        model.addAttribute("pageDescription", SeoText.description(industry.summary(), industry.challenge()));
        model.addAttribute("jsonLd", seo.breadcrumbGraph(List.of(
                seo.crumb("Home", "/"),
                seo.crumb("Industries", "/industries"),
                seo.crumb(industry.title(), null))));
        model.addAttribute("preloadImage", "/assets/industries/" + industry.imageFileName());
        return "industry-detail";
    }

    @GetMapping("/careers")
    public String careers(Model model) {
        model.addAttribute("pageTitle", "Careers at Infeara Technologies");
        model.addAttribute("pageDescription", SeoText.description(
                "Learn about working at Infeara Technologies, how we hire, what the team owns, and what a respectful recruitment process looks like for every candidate."));
        model.addAttribute("jsonLd", seo.breadcrumbGraph(List.of(
                seo.crumb("Home", "/"),
                seo.crumb("Careers", null))));
        model.addAttribute("preloadImage", "/assets/hero/inner-pages-cover.jpg");
        return "careers";
    }

    @GetMapping("/careers/open-positions")
    public String careerOpenPositions(Model model) {
        model.addAttribute("pageTitle", "Open Positions | Infeara Technologies");
        model.addAttribute("pageDescription", SeoText.description(
                "Browse current career openings at Infeara Technologies. Only verified positions are listed, so every role shown here is a confirmed vacancy."));
        model.addAttribute("jsonLd", seo.breadcrumbGraph(List.of(
                seo.crumb("Home", "/"),
                seo.crumb("Careers", "/careers"),
                seo.crumb("Open positions", null))));
        model.addAttribute("jobs", List.of());
        return "career-open-positions";
    }

    @GetMapping("/about")
    public String about(Model model) {
        model.addAttribute("pageTitle", "About Infeara Technologies");
        model.addAttribute("pageDescription", SeoText.description(
                "A collaborative technology partner built around practical problem-solving and human partnership: listen first, explain clearly, and deliver responsibly."));
        model.addAttribute("jsonLd", seo.organizationAndBreadcrumbGraph(List.of(
                seo.crumb("Home", "/"),
                seo.crumb("About Infeara", null))));
        model.addAttribute("services", content.featuredServices());
        return "about";
    }

    @GetMapping("/clients")
    public String clients(Model model) {
        model.addAttribute("pageTitle", "Our Clients | Infeara Technologies");
        model.addAttribute("pageDescription", SeoText.description(
                "Explore how Infeara Technologies approaches client collaboration and technology work, with shared context, agreed scope, and clear ownership from day one."));
        model.addAttribute("jsonLd", seo.breadcrumbGraph(List.of(
                seo.crumb("Home", "/"),
                seo.crumb("Our clients", null))));
        model.addAttribute("clientLogos", List.of());
        model.addAttribute("clientTestimonials", List.of());
        return "clients";
    }

    @GetMapping("/newsletters")
    public String newsletters(Model model) {
        model.addAttribute("pageTitle", "Newsletters | Infeara Technologies");
        model.addAttribute("pageDescription", SeoText.description(
                "Browse published Infeara Technologies newsletter editions: short, practical updates on technology questions worth asking, written for business teams."));
        model.addAttribute("jsonLd", seo.breadcrumbGraph(List.of(
                seo.crumb("Home", "/"),
                seo.crumb("About", "/about"),
                seo.crumb("Newsletters", null))));
        model.addAttribute("newsletterEditions", newsletterService.findPublished());
        return "newsletters";
    }

    @GetMapping("/newsletters/{slug}")
    public String newsletterEdition(@PathVariable String slug, Model model) {
        NewsletterEdition edition = newsletterService.findPublished(slug)
                .orElseThrow(() -> notFound("Newsletter edition"));
        model.addAttribute("edition", edition);
        model.addAttribute("pageTitle", SeoText.titleWithSuffix(edition.title(), "Infeara Newsletters"));
        model.addAttribute("pageDescription", SeoText.description(edition.excerpt(), ""));
        model.addAttribute("ogType", "article");
        model.addAttribute("ogArticlePublishedTime", seo.isoDate(edition.publishedAt()));
        model.addAttribute("jsonLd", seo.breadcrumbGraph(List.of(
                seo.crumb("Home", "/"),
                seo.crumb("Newsletters", "/newsletters"),
                seo.crumb(edition.editionLabel(), null))));
        return "newsletter-detail";
    }

    @GetMapping("/blogs")
    public String blogs(@RequestParam(required = false) String q,
                        @RequestParam(required = false) String category, Model model) {
        List<Insight> published = insightService.findPublished();
        List<Insight> filtered = filterArticles(published, q, category);
        model.addAttribute("pageTitle", "Blogs | Infeara Technologies");
        model.addAttribute("pageDescription", SeoText.description(
                "Practical blog articles on infrastructure, cybersecurity, cloud, automation, and business technology, written to give teams a clear next step."));
        model.addAttribute("jsonLd", seo.breadcrumbGraph(List.of(
                seo.crumb("Home", "/"),
                seo.crumb("Blogs", null))));
        model.addAttribute("blogs", filtered);
        model.addAttribute("query", q == null ? "" : q);
        model.addAttribute("selectedCategory", category == null ? "" : category);
        model.addAttribute("categories", published.stream().map(Insight::category).distinct().sorted().toList());
        return "blogs";
    }

    @GetMapping("/blogs/{slug}")
    public String blog(@PathVariable String slug, Model model) {
        Insight blog = insightService.findPublished(slug).orElseThrow(() -> notFound("Blog article"));
        String canonical = seo.canonical("/blogs/" + slug);
        model.addAttribute("blog", blog);
        model.addAttribute("relatedBlogs", insightService.findPublished().stream()
                .filter(item -> !item.slug().equals(slug)).limit(2).toList());
        model.addAttribute("pageTitle", SeoText.titleWithSuffix(blog.title(), "Infeara Blog"));
        model.addAttribute("pageDescription", SeoText.description(blog.excerpt(), ""));
        model.addAttribute("ogType", "article");
        model.addAttribute("ogArticlePublishedTime", seo.isoDate(blog.publishedAt()));
        model.addAttribute("jsonLd", seo.articleGraph(
                List.of(seo.crumb("Home", "/"), seo.crumb("Blogs", "/blogs"), seo.crumb(blog.title(), null)),
                "BlogPosting", blog.title(), blog.excerpt(), canonical, blog.publishedAt(), blog.author()));
        return "blog-detail";
    }

    @GetMapping("/insights")
    public String insights(@RequestParam(required = false) String q,
                           @RequestParam(required = false) String category, Model model) {
        List<Insight> published = insightService.findPublished();
        List<Insight> filtered = filterArticles(published, q, category);
        model.addAttribute("pageTitle", "Insights | Infeara Technologies");
        model.addAttribute("pageDescription", SeoText.description(
                "Practical notes on infrastructure, cybersecurity, cloud, automation, and business technology for the decisions teams face during an implementation."));
        model.addAttribute("jsonLd", seo.breadcrumbGraph(List.of(
                seo.crumb("Home", "/"),
                seo.crumb("Insights", null))));
        model.addAttribute("insights", filtered);
        model.addAttribute("query", q == null ? "" : q);
        model.addAttribute("selectedCategory", category == null ? "" : category);
        model.addAttribute("categories", published.stream().map(Insight::category).distinct().sorted().toList());
        return "insights";
    }

    /**
     * Field notes and blog articles are the same published content. The article detail view is
     * consolidated on the canonical /blogs/{slug} address, so this legacy path redirects once
     * instead of serving a duplicate page.
     */
    @GetMapping("/insights/{slug}")
    public RedirectView insight(@PathVariable String slug) {
        return movedPermanently("/blogs/" + slug, false);
    }

    @GetMapping("/contact")
    public String contact(@RequestParam(defaultValue = "false") boolean sent,
                          @RequestParam(required = false) String service, Model model) {
        addContactMetadata(model);
        model.addAttribute("jsonLd", seo.breadcrumbGraph(List.of(
                seo.crumb("Home", "/"),
                seo.crumb("Contact", null))));
        model.addAttribute("request", emptyRequest(service));
        model.addAttribute("sent", sent);
        return "contact";
    }

    @PostMapping("/contact")
    public String submitContact(@Valid @ModelAttribute("request") ConsultationRequest request,
                                BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            addContactMetadata(model);
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
            addContactMetadata(model);
            model.addAttribute("formError", "We could not send your enquiry just now. Please try again shortly.");
            return "contact";
        }
    }

    @GetMapping("/privacy")
    public String privacy(Model model) {
        model.addAttribute("pageTitle", "Privacy Notice | Infeara Technologies");
        model.addAttribute("pageDescription", SeoText.description(
                "How Infeara Technologies handles information submitted through the website: what the enquiry form collects, why, and how to request a change or deletion."));
        return "privacy";
    }

    @GetMapping("/terms")
    public String terms(Model model) {
        model.addAttribute("pageTitle", "Terms of Use | Infeara Technologies");
        model.addAttribute("pageDescription", SeoText.description(
                "Terms for using the Infeara Technologies website, including how to understand its content, enquiries, and links to external sites."));
        return "terms";
    }

    // --- Legacy URLs: one-hop permanent redirects to the preferred URL. -------------------------

    @GetMapping("/index.html")
    public RedirectView legacyIndex() {
        return movedPermanently("/", false);
    }

    @GetMapping({"/services.html", "/industries.html", "/careers.html", "/about.html", "/clients.html",
            "/insights.html", "/blogs.html", "/newsletters.html", "/contact.html", "/privacy.html",
            "/terms.html"})
    public RedirectView legacyHtmlPage(HttpServletRequest request) {
        return movedPermanently(requestPath(request).replaceFirst("\\.html$", ""), true);
    }

    @GetMapping("/partners")
    public RedirectView legacyPartners() {
        return movedPermanently("/clients", true);
    }

    @GetMapping("/partners.html")
    public RedirectView legacyPartnersHtml() {
        return movedPermanently("/clients", true);
    }

    @GetMapping("/insight-{slug}.html")
    public RedirectView oldInsightRoute(@PathVariable String slug) {
        // Field-note detail views are consolidated on /blogs/{slug}, so this old file redirects
        // straight there in one hop instead of chaining through /insights/{slug}.
        return movedPermanently("/blogs/" + slug, false);
    }

    @GetMapping("/blog-{slug}.html")
    public RedirectView oldBlogRoute(@PathVariable String slug) {
        return movedPermanently("/blogs/" + slug, false);
    }

    @GetMapping("/newsletter-{slug}.html")
    public RedirectView oldNewsletterRoute(@PathVariable String slug) {
        return movedPermanently("/newsletters/" + slug, false);
    }

    private void addContactMetadata(Model model) {
        model.addAttribute("pageTitle", "Contact Infeara Technologies");
        model.addAttribute("pageDescription", SeoText.description(
                "Tell Infeara Technologies what you are working toward and request a consultation. Share the systems, constraints, and outcomes that matter."));
    }

    private List<Insight> filterArticles(List<Insight> articles, String query, String category) {
        return articles.stream()
                .filter(article -> query == null || query.isBlank()
                        || (article.title() + " " + article.excerpt() + " " + article.content())
                                .toLowerCase(java.util.Locale.ROOT).contains(query.trim().toLowerCase(java.util.Locale.ROOT)))
                .filter(article -> category == null || category.isBlank()
                        || article.category().equalsIgnoreCase(category))
                .toList();
    }

    private List<Map<String, String>> technologyLogos() {
        return List.of(
                Map.of("name", "Microsoft", "logoPath", "/assets/technologies/microsoft.svg"),
                Map.of("name", "Google", "logoPath", "/assets/technologies/google.svg"),
                Map.of("name", "Zoho", "logoPath", "/assets/technologies/zoho.svg"),
                Map.of("name", "Sophos", "logoPath", "/assets/technologies/sophos.png"),
                Map.of("name", "FortiGate", "logoPath", "/assets/technologies/fortinet.svg"),
                Map.of("name", "SonicWall", "logoPath", "/assets/technologies/sonicwall.svg"),
                Map.of("name", "NETGEAR", "logoPath", "/assets/technologies/netgear.svg"),
                Map.of("name", "Dell", "logoPath", "/assets/technologies/dell.svg"),
                Map.of("name", "Lenovo", "logoPath", "/assets/technologies/lenovo.svg"),
                Map.of("name", "Acer", "logoPath", "/assets/technologies/acer.svg"),
                Map.of("name", "HP", "logoPath", "/assets/technologies/hp.svg")
        );
    }

    private ConsultationRequest emptyRequest(String selectedService) {
        return new ConsultationRequest("", "", "", "", selectedService == null ? "" : selectedService,
                "", "", "/contact", "", false);
    }

    /** One-hop permanent redirect used for every URL that has moved. */
    private RedirectView movedPermanently(String target, boolean keepQueryString) {
        RedirectView view = new RedirectView(target, true);
        view.setStatusCode(HttpStatus.MOVED_PERMANENTLY);
        view.setPropagateQueryParams(keepQueryString);
        return view;
    }

    private String requestPath(HttpServletRequest request) {
        String uri = request.getRequestURI();
        if (uri == null || uri.isBlank()) {
            return "/";
        }
        String contextPath = request.getContextPath();
        if (contextPath != null && !contextPath.isEmpty() && uri.startsWith(contextPath)) {
            uri = uri.substring(contextPath.length());
        }
        if (uri.isEmpty()) {
            return "/";
        }
        return uri.startsWith("/") ? uri : "/" + uri;
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
