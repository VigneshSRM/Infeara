package com.infeara.site.cms;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * The network address a sign-in attempt came from, used only to make lockouts harder to spread
 * across many usernames.
 *
 * <p>The value comes from the container, never from a caller-controlled header, so an attacker
 * cannot escape a lockout by inventing {@code X-Forwarded-For} values. Behind a load balancer,
 * enable {@code server.forward-headers-strategy=framework} so the container fills in the address
 * that the proxy observed.</p>
 */
final class ClientAddress {
    private static final String UNKNOWN = "unknown";

    private ClientAddress() {
    }

    /** The caller's address, or {@code "unknown"} outside a request (for example in a test). */
    static String current() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes instanceof ServletRequestAttributes servletAttributes) {
            return of(servletAttributes.getRequest());
        }
        return UNKNOWN;
    }

    /** The caller's address for a request that is already in hand. */
    static String of(HttpServletRequest request) {
        if (request == null) {
            return UNKNOWN;
        }
        String remote = request.getRemoteAddr();
        return remote == null || remote.isBlank() ? UNKNOWN : remote;
    }
}
