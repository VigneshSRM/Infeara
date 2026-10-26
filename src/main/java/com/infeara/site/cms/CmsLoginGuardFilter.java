package com.infeara.site.cms;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Rejects sign-in posts whose fields are absurdly large before any password hashing happens.
 *
 * <p>BCrypt reads at most 72 bytes of input and the underlying library throws on anything longer.
 * Without this guard an over-long password would surface as a server error instead of a normal
 * rejection, and every oversized post would still cost a hash computation. The guard also keeps
 * multi-megabyte usernames out of the lockout map.</p>
 */
final class CmsLoginGuardFilter extends OncePerRequestFilter {
    /** BCrypt's input limit, in bytes rather than characters. */
    private static final int MAX_PASSWORD_BYTES = 72;
    private static final int MAX_USERNAME_LENGTH = 254;

    private final String loginPath;

    CmsLoginGuardFilter(String loginPath) {
        this.loginPath = loginPath;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !("POST".equalsIgnoreCase(request.getMethod()) && loginPath.equals(pathWithinApplication(request)));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        boolean tooLong = username != null && username.length() > MAX_USERNAME_LENGTH;
        if (password != null && password.getBytes(StandardCharsets.UTF_8).length > MAX_PASSWORD_BYTES) {
            tooLong = true;
        }
        if (tooLong) {
            // Same destination as any other rejected attempt: the caller learns nothing new.
            response.sendRedirect(request.getContextPath() + loginPath + "?error");
            return;
        }
        chain.doFilter(request, response);
    }

    private static String pathWithinApplication(HttpServletRequest request) {
        String uri = request.getRequestURI();
        if (uri == null || uri.isEmpty()) {
            return "/";
        }
        String contextPath = request.getContextPath();
        return contextPath == null || contextPath.isEmpty() ? uri : uri.substring(contextPath.length());
    }
}
