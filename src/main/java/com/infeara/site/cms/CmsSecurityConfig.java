package com.infeara.site.cms;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.StaticHeadersWriter;

/**
 * Security for the content manager, scoped so the public marketing site is untouched.
 *
 * <p>Two filter chains are declared. The first matches only {@code /cms} and everything below it
 * and carries every control an administration area needs; the second matches everything else and
 * only makes the public pages reachable, exactly as they were before. Keeping the chains separate
 * means adding the CMS cannot silently change how the public site, its forms or its caching behave.
 * </p>
 *
 * <p>On the content-manager chain: form login with a fixed processing URL, CSRF tokens on every
 * state-changing request, session fixation protection, a strict Content-Security-Policy that allows
 * no third-party origin and no inline code, frame denial, no referrer leakage, and a noindex header
 * so the sign-in page can never be crawled or archived.</p>
 */
@Configuration
@EnableConfigurationProperties(CmsProperties.class)
public class CmsSecurityConfig {
    static final String LOGIN_PAGE = "/cms/login";
    static final String HOME = "/cms";
    private static final String SESSION_COOKIE = "CMS_SESSION";
    private static final String REJECTED = LOGIN_PAGE + "?error";
    private static final String LOCKED = LOGIN_PAGE + "?locked";

    /**
     * Nothing from another origin and nothing inline: the sign-in page loads one stylesheet, one
     * script and one image, all served by this application. A stolen or injected page cannot call
     * home, cannot frame the form and cannot exfiltrate through a referrer.
     */
    private static final String CONTENT_SECURITY_POLICY = String.join("; ",
            "default-src 'none'",
            "style-src 'self'",
            "script-src 'self'",
            "img-src 'self' data:",
            "font-src 'self'",
            "connect-src 'self'",
            "form-action 'self'",
            "frame-ancestors 'none'",
            "base-uri 'none'");

    private static final String PERMISSIONS_POLICY =
            "accelerometer=(), camera=(), geolocation=(), gyroscope=(), microphone=(), payment=(), usb=()";

    @Bean
    @Order(1)
    public SecurityFilterChain contentManagerFilterChain(HttpSecurity http, LoginAttemptService attempts)
            throws Exception {
        http
                .securityMatcher(HOME, HOME + "/**")
                .authorizeHttpRequests(requests -> requests
                        .requestMatchers(HttpMethod.GET, LOGIN_PAGE).permitAll()
                        .requestMatchers(HttpMethod.POST, LOGIN_PAGE).permitAll()
                        .anyRequest().hasRole("CMS_ADMIN"))
                .formLogin(login -> login
                        .loginPage(LOGIN_PAGE)
                        .loginProcessingUrl(LOGIN_PAGE)
                        .successHandler((request, response, authentication) -> {
                            attempts.recordSuccess(ClientAddress.of(request), authentication.getName());
                            // Always land on the workspace: a saved request is never replayed, so a
                            // crafted link cannot bounce a fresh session somewhere else.
                            response.sendRedirect(request.getContextPath() + HOME);
                        })
                        .failureHandler((request, response, exception) -> {
                            if (exception instanceof BadCredentialsException) {
                                attempts.recordFailure(ClientAddress.of(request), request.getParameter("username"));
                            }
                            boolean lockedOut = exception instanceof LockedException;
                            response.sendRedirect(request.getContextPath() + (lockedOut ? LOCKED : REJECTED));
                        }))
                .logout(logout -> logout
                        .logoutUrl(HOME + "/logout")
                        .logoutSuccessUrl(LOGIN_PAGE + "?signedOut")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies(SESSION_COOKIE))
                // A new session identifier after sign-in closes off session fixation.
                .sessionManagement(session -> session.sessionFixation(fixation -> fixation.migrateSession()))
                .csrf(Customizer.withDefaults())
                .addFilterBefore(new CmsLoginGuardFilter(LOGIN_PAGE), UsernamePasswordAuthenticationFilter.class)
                .headers(headers -> headers
                        .contentTypeOptions(Customizer.withDefaults())
                        .frameOptions(frame -> frame.deny())
                        .addHeaderWriter(new StaticHeadersWriter("Content-Security-Policy", CONTENT_SECURITY_POLICY))
                        .addHeaderWriter(new StaticHeadersWriter("Referrer-Policy", "no-referrer"))
                        .addHeaderWriter(new StaticHeadersWriter("Permissions-Policy", PERMISSIONS_POLICY))
                        .addHeaderWriter(new StaticHeadersWriter("Cross-Origin-Opener-Policy", "same-origin"))
                        .addHeaderWriter(new StaticHeadersWriter("Cross-Origin-Resource-Policy", "same-origin"))
                        .addHeaderWriter(new StaticHeadersWriter("X-Robots-Tag", "noindex, nofollow")));
        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain publicSiteFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(requests -> requests.anyRequest().permitAll())
                // The public pages hold no signed-in state, so a forged request has no authority to
                // borrow; the contact form keeps posting without a token, exactly as it did before.
                .csrf(csrf -> csrf.disable())
                // Public HTML keeps the caching behaviour the performance budget depends on.
                .headers(headers -> headers.cacheControl(cache -> cache.disable()));
        return http.build();
    }

    /**
     * BCrypt with cost 12: slow enough to make offline guessing expensive, with a per-password salt
     * generated by the encoder so identical passwords never share a hash.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
