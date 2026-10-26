# CI server diagnostics

Written by the workflow when a gate fails, because job logs are not readable here.

## Response for /cms/login

```
status 500, 0 bytes

```

## Server log, around the failure

```
56:	at jakarta.servlet.http.HttpServlet.service(HttpServlet.java:622) ~[tomcat-embed-core-11.0.27.jar!/:6.1]
57:	at org.springframework.web.servlet.FrameworkServlet.service(FrameworkServlet.java:874) ~[spring-webmvc-7.0.10.jar!/:7.0.10]
58-	at jakarta.servlet.http.HttpServlet.service(HttpServlet.java:710) ~[tomcat-embed-core-11.0.27.jar!/:6.1]
59-	at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:132) ~[tomcat-embed-core-11.0.27.jar!/:11.0.27]
60-	at org.apache.tomcat.websocket.server.WsFilter.doFilter(WsFilter.java:59) ~[tomcat-embed-websocket-11.0.27.jar!/:11.0.27]
61-	at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:111) ~[tomcat-embed-core-11.0.27.jar!/:11.0.27]
62-	at com.infeara.site.seo.SeoRequestFilter.doFilterInternal(SeoRequestFilter.java:57) ~[!/:1.0.0]
63-	at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:116) ~[spring-web-7.0.10.jar!/:7.0.10]
64-	at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:111) ~[tomcat-embed-core-11.0.27.jar!/:11.0.27]
65-	at org.springframework.web.filter.CompositeFilter$VirtualFilterChain.doFilter(CompositeFilter.java:108) ~[spring-web-7.0.10.jar!/:7.0.10]
66-	at org.springframework.security.web.FilterChainProxy.lambda$doFilterInternal$2(FilterChainProxy.java:235) ~[spring-security-web-7.1.2.jar!/:7.1.2]
67-	at org.springframework.security.web.FilterChainProxy$VirtualFilterChain.doFilter(FilterChainProxy.java:378) ~[spring-security-web-7.1.2.jar!/:7.1.2]
68-	at org.springframework.security.web.access.intercept.AuthorizationFilter.doFilter(AuthorizationFilter.java:101) ~[spring-security-web-7.1.2.jar!/:7.1.2]
69-	at org.springframework.security.web.FilterChainProxy$VirtualFilterChain.doFilter(FilterChainProxy.java:387) ~[spring-security-web-7.1.2.jar!/:7.1.2]
70-	at org.springframework.security.web.access.ExceptionTranslationFilter.doFilter(ExceptionTranslationFilter.java:126) ~[spring-security-web-7.1.2.jar!/:7.1.2]
71-	at org.springframework.security.web.access.ExceptionTranslationFilter.doFilter(ExceptionTranslationFilter.java:120) ~[spring-security-web-7.1.2.jar!/:7.1.2]
72-	at org.springframework.security.web.FilterChainProxy$VirtualFilterChain.doFilter(FilterChainProxy.java:387) ~[spring-security-web-7.1.2.jar!/:7.1.2]
73-	at org.springframework.security.web.authentication.AnonymousAuthenticationFilter.doFilter(AnonymousAuthenticationFilter.java:101) ~[spring-security-web-7.1.2.jar!/:7.1.2]
74-	at org.springframework.security.web.FilterChainProxy$VirtualFilterChain.doFilter(FilterChainProxy.java:387) ~[spring-security-web-7.1.2.jar!/:7.1.2]
75-	at org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestFilter.doFilter(SecurityContextHolderAwareRequestFilter.java:181) ~[spring-security-web-7.1.2.jar!/:7.1.2]
76-	at org.springframework.security.web.FilterChainProxy$VirtualFilterChain.doFilter(FilterChainProxy.java:387) ~[spring-security-web-7.1.2.jar!/:7.1.2]
77-	at org.springframework.security.web.savedrequest.RequestCacheAwareFilter.doFilter(RequestCacheAwareFilter.java:63) ~[spring-security-web-7.1.2.jar!/:7.1.2]
78-	at org.springframework.security.web.FilterChainProxy$VirtualFilterChain.doFilter(FilterChainProxy.java:387) ~[spring-security-web-7.1.2.jar!/:7.1.2]
79-	at org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter.doFilter(AbstractAuthenticationProcessingFilter.java:246) ~[spring-security-web-7.1.2.jar!/:7.1.2]
80-	at org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter.doFilter(AbstractAuthenticationProcessingFilter.java:240) ~[spring-security-web-7.1.2.jar!/:7.1.2]
81-	at org.springframework.security.web.FilterChainProxy$VirtualFilterChain.doFilter(FilterChainProxy.java:387) ~[spring-security-web-7.1.2.jar!/:7.1.2]
82-	at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:101) ~[spring-web-7.0.10.jar!/:7.0.10]
83-	at org.springframework.security.web.FilterChainProxy$VirtualFilterChain.doFilter(FilterChainProxy.java:387) ~[spring-security-web-7.1.2.jar!/:7.1.2]
84-	at org.springframework.security.web.authentication.logout.LogoutFilter.doFilter(LogoutFilter.java:110) ~[spring-security-web-7.1.2.jar!/:7.1.2]
85-	at org.springframework.security.web.authentication.logout.LogoutFilter.doFilter(LogoutFilter.java:96) ~[spring-security-web-7.1.2.jar!/:7.1.2]
86-	at org.springframework.security.web.FilterChainProxy$VirtualFilterChain.doFilter(FilterChainProxy.java:387) ~[spring-security-web-7.1.2.jar!/:7.1.2]
87-	at org.springframework.security.web.csrf.CsrfFilter.doFilterInternal(CsrfFilter.java:118) ~[spring-security-web-7.1.2.jar!/:7.1.2]
```

## CMS gate output

```
CMS sign-in gate against http://localhost:8080

  FAIL sign-in page did not return 200 (status 500)
  FAIL sign-in page is missing the noindex robots meta tag
  FAIL sign-in form has no CSRF token
  FAIL sign-in form does not post to /cms/login
  FAIL password field is missing autocomplete or echoes a value
  ok   sign-in page loads nothing from another origin
  ok   Content-Security-Policy denies other origins and framing
  ok   X-Frame-Options is DENY
  ok   X-Content-Type-Options is nosniff
  ok   Referrer-Policy is no-referrer
  ok   administration area sends X-Robots-Tag noindex
  ok   administration responses are not cacheable
  ok   the workspace redirects an anonymous caller to the sign-in page
  ok   a sign-in post without a CSRF token is refused with 403
  FAIL a wrong password was not rejected with the generic error (location: )
  ok   a failed sign-in does not open the workspace
  FAIL an unknown username produced a different answer (location: )
  FAIL the correct credentials did not reach the workspace (location: )
  FAIL the session cookie is missing, renamed, or not HttpOnly
  FAIL the session cookie does not set SameSite=Lax
  FAIL the workspace did not load for the signed-in account (status 302)
  ok   a sign-out post without a CSRF token is refused
  ok   a GET request cannot sign a session out
  FAIL signing out did not reach the sign-in page (location: )
  ok   the workspace is closed again after signing out
  FAIL repeated failures never locked the account (last location: )
  FAIL the lockout blocked a legitimate sign-in (location: )

CMS sign-in gate FAILED (14 of 27 checks failed)
```
