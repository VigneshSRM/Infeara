package com.infeara.site.cms;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

import jakarta.servlet.http.HttpSession;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.infeara.site.content.InsightService;
import com.infeara.site.content.NewsletterService;
import com.infeara.site.content.PageCopyService;
import com.infeara.site.content.WebsiteContent;

/**
 * The sign-in page and the workspace behind it.
 *
 * <p>The controller never sees a password: authentication happens in the security filter chain
 * before either method runs. Both pages are reachable only through that chain, which is what puts
 * the strict headers, the CSRF token and the noindex directives on them.</p>
 */
@Controller
@RequestMapping("/cms")
public class CmsController {
    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm").withZone(ZoneId.systemDefault());

    private final WebsiteContent content;
    private final InsightService insights;
    private final NewsletterService newsletters;
    private final PageCopyService pageCopies;
    private final CmsProperties properties;
    private final CmsUserDetailsService accounts;

    public CmsController(WebsiteContent content, InsightService insights, NewsletterService newsletters,
                         PageCopyService pageCopies, CmsProperties properties, CmsUserDetailsService accounts) {
        this.content = content;
        this.insights = insights;
        this.newsletters = newsletters;
        this.pageCopies = pageCopies;
        this.properties = properties;
        this.accounts = accounts;
    }

    /** The sign-in page. Rendered for anonymous callers only; a signed-in caller is sent on. */
    @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute("signInConfigured", accounts.isConfigured());
        return "cms/login";
    }

    /** The workspace, reachable only with a signed-in CMS administrator. */
    @GetMapping
    public String dashboard(Authentication authentication, HttpSession session, Model model) {
        model.addAttribute("accountName", authentication.getName());
        model.addAttribute("roleSummary", authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .sorted()
                .reduce((left, right) -> left + ", " + right)
                .orElse("none"));
        model.addAttribute("sessionStarted", TIME_FORMAT.format(Instant.ofEpochMilli(session.getCreationTime())));
        model.addAttribute("sessionTimeout", humanise(Duration.ofSeconds(session.getMaxInactiveInterval())));
        model.addAttribute("lockoutThreshold", properties.resolvedMaxAttempts());
        model.addAttribute("lockoutMinutes", properties.resolvedLockoutMinutes());
        model.addAttribute("secretStoredAsHash", properties.usesPreHashedSecret());
        model.addAttribute("stats", List.of(
                new Stat("Service areas", content.services().size(), "top-level offers"),
                new Stat("Capability pages", content.allSpecialistServices().size(), "one page each"),
                new Stat("Industry pages", content.industries().size(), "sector by sector"),
                new Stat("Field notes", insights.findPublished().size(), "published insights"),
                new Stat("Newsletters", newsletters.findPublished().size(), "editions sent"),
                new Stat("Page copy records", pageCopies.size(), "editorial entries")));
        return "cms/dashboard";
    }

    private static String humanise(Duration duration) {
        long minutes = duration.toMinutes();
        if (minutes < 60) {
            return minutes + (minutes == 1 ? " minute" : " minutes");
        }
        long hours = duration.toHours();
        return hours + (hours == 1 ? " hour" : " hours");
    }

    /** One figure on the workspace, in a shape the template can read without extra handling. */
    public record Stat(String label, int value, String detail) {
    }
}
