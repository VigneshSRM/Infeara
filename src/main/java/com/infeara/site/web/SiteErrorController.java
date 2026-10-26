package com.infeara.site.web;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Serves the site's own error page for status pages instead of the container's default page.
 *
 * <p>Unknown URLs already answer 404 rather than a soft 200; this makes the response a branded
 * page with a way back into the site, while keeping the status code a crawler sees. Every error
 * response is sent with {@code X-Robots-Tag: noindex, nofollow} by {@code SeoRequestFilter}.</p>
 */
@Controller
public class SiteErrorController implements ErrorController {

    @RequestMapping("/error")
    public String error(HttpServletRequest request, Model model) {
        Object attribute = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        int status = attribute instanceof Integer value ? value : HttpStatus.NOT_FOUND.value();
        model.addAttribute("status", status);
        model.addAttribute("errorMessage", HttpStatus.resolve(status) == null
                ? "Something went wrong" : HttpStatus.resolve(status).getReasonPhrase());
        model.addAttribute("currentYear", java.time.Year.now().getValue());
        return "error";
    }
}
