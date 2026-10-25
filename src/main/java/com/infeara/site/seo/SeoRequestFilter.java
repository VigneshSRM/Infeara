package com.infeara.site.seo;

import java.io.IOException;
import java.util.Locale;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Keeps URL variants out of the index and stops non-production deployments from being crawled.
 *
 * <ul>
 *   <li>{@code /path/} and {@code /path/index.html} permanently redirect (301, one hop) to {@code /path}.</li>
 *   <li>The bare domain ({@code infeara.com}) permanently redirects (301) to the canonical
 *       {@code www} host, so only one hostname is ever indexed.</li>
 *   <li>Deployments that are not indexable send {@code X-Robots-Tag: noindex, nofollow}.</li>
 *   <li>JSON API responses are marked noindex.</li>
 * </ul>
 */
@Component
public class SeoRequestFilter extends OncePerRequestFilter {
    private static final String INDEX_SUFFIX = "/index.html";

    private final SeoProperties properties;

    public SeoRequestFilter(SeoProperties properties) {
        this.properties = properties;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String path = requestPath(request);
        String hostRedirect = canonicalHostRedirect(request, path);
        if (hostRedirect != null && isReadRequest(request)) {
            response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
            response.setHeader("Location", hostRedirect);
            response.setHeader("Cache-Control", "public, max-age=3600");
            return;
        }
        String canonicalPath = canonicalPath(path);
        if (canonicalPath != null && isReadRequest(request)) {
            String query = request.getQueryString();
            String location = query == null || query.isBlank() ? canonicalPath : canonicalPath + "?" + query;
            response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
            response.setHeader("Location", location);
            response.setHeader("Cache-Control", "public, max-age=3600");
            return;
        }
        if (!properties.isIndexable() || path.startsWith("/api/")) {
            response.setHeader("X-Robots-Tag", "noindex, nofollow");
        }
        chain.doFilter(request, response);
        // Error responses are utility pages, never search results.
        if (response.getStatus() >= 400) {
            response.setHeader("X-Robots-Tag", "noindex, nofollow");
        }
    }

    /**
     * Returns the canonical URL when the request arrived on the bare domain of a {@code www}
     * canonical origin, otherwise {@code null}. Other hostnames are left alone: a local or preview
     * deployment must not be redirected to production just because it shares the application.
     */
    private String canonicalHostRedirect(HttpServletRequest request, String path) {
        String canonicalHost = properties.canonicalHost();
        if (!canonicalHost.startsWith("www.")) {
            return null;
        }
        String bareDomain = canonicalHost.substring("www.".length());
        String requestHost = request.getServerName();
        if (requestHost == null || !requestHost.toLowerCase(Locale.ROOT).equals(bareDomain)) {
            return null;
        }
        String query = request.getQueryString();
        return properties.baseUrl() + path + (query == null || query.isBlank() ? "" : "?" + query);
    }

    private boolean isReadRequest(HttpServletRequest request) {
        String method = request.getMethod();
        return "GET".equalsIgnoreCase(method) || "HEAD".equalsIgnoreCase(method);
    }

    /**
     * Returns the preferred path when the request uses a variant URL, otherwise {@code null}.
     * The site serves every page without a trailing slash and never from an {@code index.html} file.
     */
    private String canonicalPath(String path) {
        String candidate = path;
        if (candidate.endsWith(INDEX_SUFFIX)) {
            candidate = candidate.substring(0, candidate.length() - "index.html".length());
        }
        while (candidate.length() > 1 && candidate.endsWith("/")) {
            candidate = candidate.substring(0, candidate.length() - 1);
        }
        if (candidate.isEmpty()) {
            candidate = "/";
        }
        return candidate.equals(path) ? null : candidate;
    }

    private String requestPath(HttpServletRequest request) {
        String uri = request.getRequestURI();
        if (uri == null || uri.isEmpty()) {
            return "/";
        }
        String contextPath = request.getContextPath();
        if (contextPath != null && !contextPath.isEmpty() && uri.startsWith(contextPath)) {
            uri = uri.substring(contextPath.length());
        }
        return uri.isEmpty() ? "/" : uri;
    }
}
