# CI server diagnostics

Written by the workflow when a gate fails, because job logs are not readable here.

## Response for /cms/login

```
HTTP/1.1 200 
X-Robots-Tag: noindex, nofollow
Set-Cookie: CMS_SESSION=8BCAA01B8E531A3AA0E9BA30948DD311; Path=/; HttpOnly; SameSite=Lax
X-Content-Type-Options: nosniff
X-XSS-Protection: 0
Cache-Control: no-cache, no-store, max-age=0, must-revalidate
Pragma: no-cache
Expires: 0
X-Frame-Options: DENY
Content-Security-Policy: default-src 'none'; style-src 'self'; script-src 'self'; img-src 'self' data:; font-src 'self'; connect-src 'self'; form-action 'self'; frame-ancestors 'none'; base-uri 'none'
Referrer-Policy: no-referrer
Permissions-Policy: accelerometer=(), camera=(), geolocation=(), gyroscope=(), microphone=(), payment=(), usb=()
Cross-Origin-Opener-Policy: same-origin
Cross-Origin-Resource-Policy: same-origin
vary: accept-encoding
Content-Type: text/html;charset=UTF-8
Content-Language: en
Transfer-Encoding: chunked
Date: Mon, 26 Oct 2026 10:48:45 GMT

<!doctype html>
<html lang="en">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Sign in | Infeara Content Manager</title>
  <!-- A sign-in page is never a search result, never archived and never used as a referrer. -->
  <meta name="robots" conte
```

## First lines of the server log

```

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

 :: Spring Boot ::                (v4.1.2)

2026-10-26T10:48:36.360Z  INFO 2171 --- [infeara-website] [           main] com.infeara.site.InfearaApplication      : Starting InfearaApplication v1.0.0 using Java 27.0.1.0.2 with PID 2171 (/home/runner/work/Infeara/Infeara/target/infeara-website-1.0.0.jar started by runner in /home/runner/work/Infeara/Infeara)
2026-10-26T10:48:36.363Z  INFO 2171 --- [infeara-website] [           main] com.infeara.site.InfearaApplication      : No active profile set, falling back to 1 default profile: "default"
2026-10-26T10:48:37.404Z  INFO 2171 --- [infeara-website] [           main] o.s.boot.tomcat.TomcatWebServer          : Tomcat initialized with port 8080 (http)
2026-10-26T10:48:37.417Z  INFO 2171 --- [infeara-website] [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
2026-10-26T10:48:37.417Z  INFO 2171 --- [infeara-website] [           main] o.apache.catalina.core.StandardEngine    : Starting Servlet engine: [Apache Tomcat/11.0.27]
2026-10-26T10:48:37.437Z  INFO 2171 --- [infeara-website] [           main] b.w.c.s.WebApplicationContextInitializer : Root WebApplicationContext: initialization completed in 996 ms
2026-10-26T10:48:37.995Z  WARN 2171 --- [infeara-website] [           main] c.i.site.cms.CmsUserDetailsService       : CMS sign-in is using a plain secret from the environment; prefer CMS_ADMIN_PASSWORD_HASH so the secret is never readable by the application process
2026-10-26T10:48:38.081Z  INFO 2171 --- [infeara-website] [           main] r$InitializeUserDetailsManagerConfigurer : Global AuthenticationManager configured with UserDetailsService bean with name cmsUserDetailsService
2026-10-26T10:48:38.275Z  INFO 2171 --- [infeara-website] [           main] o.s.b.w.a.WelcomePageHandlerMapping      : Adding welcome page: class path resource [static/index.html]
2026-10-26T10:48:38.526Z  INFO 2171 --- [infeara-website] [           main] o.s.boot.tomcat.TomcatWebServer          : Tomcat started on port 8080 (http) with context path '/'
2026-10-26T10:48:38.537Z  INFO 2171 --- [infeara-website] [           main] com.infeara.site.InfearaApplication      : Started InfearaApplication in 2.603 seconds (process running for 3.056)
2026-10-26T10:48:39.573Z  INFO 2171 --- [infeara-website] [nio-8080-exec-1] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring DispatcherServlet 'dispatcherServlet'
2026-10-26T10:48:39.573Z  INFO 2171 --- [infeara-website] [nio-8080-exec-1] o.s.web.servlet.DispatcherServlet        : Initializing Servlet 'dispatcherServlet'
2026-10-26T10:48:39.574Z  INFO 2171 --- [infeara-website] [nio-8080-exec-1] o.s.web.servlet.DispatcherServlet        : Completed initialization in 0 ms
2026-10-26T10:48:43.823Z  INFO 2171 --- [infeara-website] [nio-8080-exec-6] com.infeara.site.cms.CmsSecurityConfig   : CMS sign-in rejected: lockedOut=false type=BadCredentialsException cause=none
2026-10-26T10:48:44.384Z  INFO 2171 --- [infeara-website] [nio-8080-exec-6] com.infeara.site.cms.CmsSecurityConfig   : CMS sign-in rejected: lockedOut=false type=BadCredentialsException cause=UsernameNotFoundException
2026-10-26T10:48:44.407Z  INFO 2171 --- [infeara-website] [nio-8080-exec-2] com.infeara.site.cms.CmsSecurityConfig   : CMS sign-in rejected: lockedOut=false type=BadCredentialsException cause=none
2026-10-26T10:48:44.728Z  INFO 2171 --- [infeara-website] [nio-8080-exec-7] com.infeara.site.cms.CmsSecurityConfig   : CMS sign-in rejected: lockedOut=false type=BadCredentialsException cause=UsernameNotFoundException
2026-10-26T10:48:44.994Z  INFO 2171 --- [infeara-website] [nio-8080-exec-6] com.infeara.site.cms.CmsSecurityConfig   : CMS sign-in rejected: lockedOut=false type=BadCredentialsException cause=UsernameNotFoundException
2026-10-26T10:48:45.261Z  INFO 2171 --- [infeara-website] [nio-8080-exec-9] com.infeara.site.cms.CmsSecurityConfig   : CMS sign-in rejected: lockedOut=false type=BadCredentialsException cause=UsernameNotFoundException
2026-10-26T10:48:45.531Z  INFO 2171 --- [infeara-website] [nio-8080-exec-2] com.infeara.site.cms.CmsSecurityConfig   : CMS sign-in rejected: lockedOut=false type=BadCredentialsException cause=UsernameNotFoundException
2026-10-26T10:48:45.796Z  WARN 2171 --- [infeara-website] [nio-8080-exec-5] c.infeara.site.cms.LoginAttemptService   : CMS sign-in locked out for account account:intruder for 15 minute(s) after 5 failure(s)
2026-10-26T10:48:45.797Z  INFO 2171 --- [infeara-website] [nio-8080-exec-5] com.infeara.site.cms.CmsSecurityConfig   : CMS sign-in rejected: lockedOut=false type=BadCredentialsException cause=UsernameNotFoundException
2026-10-26T10:48:45.809Z ERROR 2171 --- [infeara-website] [nio-8080-exec-7] w.a.UsernamePasswordAuthenticationFilter : An internal error occurred while trying to authenticate the user.

org.springframework.security.authentication.InternalAuthenticationServiceException: Too many failed sign-in attempts
	at org.springframework.security.authentication.dao.DaoAuthenticationProvider.retrieveUser(DaoAuthenticationProvider.java:121) ~[spring-security-core-7.1.2.jar!/:7.1.2]
	at org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider.authenticate(AbstractUserDetailsAuthenticationProvider.java:145) ~[spring-security-core-7.1.2.jar!/:7.1.2]
	at org.springframework.security.authentication.ProviderManager.authenticate(ProviderManager.java:185) ~[spring-security-core-7.1.2.jar!/:7.1.2]
	at org.springframework.security.authentication.ProviderManager.authenticate(ProviderManager.java:217) ~[spring-security-core-7.1.2.jar!/:7.1.2]
	at org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.attemptAuthentication(UsernamePasswordAuthenticationFilter.java:87) ~[spring-security-web-7.1.2.jar!/:7.1.2]
	at org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter.doFilter(AbstractAuthenticationProcessingFilter.java:250) ~[spring-security-web-7.1.2.jar!/:7.1.2]
	at org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter.doFilter(AbstractAuthenticationProcessingFilter.java:240) ~[spring-security-web-7.1.2.jar!/:7.1.2]
	at org.springframework.security.web.FilterChainProxy$VirtualFilterChain.doFilter(FilterChainProxy.java:387) ~[spring-security-web-7.1.2.jar!/:7.1.2]
	at com.infeara.site.cms.CmsLoginGuardFilter.doFilterInternal(CmsLoginGuardFilter.java:51) ~[!/:1.0.0]
	at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:116) ~[spring-web-7.0.10.jar!/:7.0.10]
	at org.springframework.security.web.FilterChainProxy$VirtualFilterChain.doFilter(FilterChainProxy.java:387) ~[spring-security-web-7.1.2.jar!/:7.1.2]
	at org.springframework.security.web.authentication.logout.LogoutFilter.doFilter(LogoutFilter.java:110) ~[spring-security-web-7.1.2.jar!/:7.1.2]
	at org.springframework.security.web.authentication.logout.LogoutFilter.doFilter(LogoutFilter.java:96) ~[spring-security-web-7.1.2.jar!/:7.1.2]
	at org.springframework.security.web.FilterChainProxy$VirtualFilterChain.doFilter(FilterChainProxy.java:387) ~[spring-security-web-7.1.2.jar!/:7.1.2]
	at org.springframework.security.web.csrf.CsrfFilter.doFilterInternal(CsrfFilter.java:135) ~[spring-security-web-7.1.2.jar!/:7.1.2]
	at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:116) ~[spring-web-7.0.10.jar!/:7.0.10]
	at org.springframework.security.web.FilterChainProxy$VirtualFilterChain.doFilter(FilterChainProxy.java:387) ~[spring-security-web-7.1.2.jar!/:7.1.2]
	at org.springframework.security.web.header.HeaderWriterFilter.doHeadersAfter(HeaderWriterFilter.java:91) ~[spring-security-web-7.1.2.jar!/:7.1.2]
	at org.springframework.security.web.header.HeaderWriterFilter.doFilterInternal(HeaderWriterFilter.java:76) ~[spring-security-web-7.1.2.jar!/:7.1.2]
	at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:116) ~[spring-web-7.0.10.jar!/:7.0.10]
	at org.springframework.security.web.FilterChainProxy$VirtualFilterChain.doFilter(FilterChainProxy.java:387) ~[spring-security-web-7.1.2.jar!/:7.1.2]
	at org.springframework.security.web.context.SecurityContextHolderFilter.doFilter(SecurityContextHolderFilter.java:82) ~[spring-security-web-7.1.2.jar!/:7.1.2]
	at org.springframework.security.web.context.SecurityContextHolderFilter.doFilter(SecurityContextHolderFilter.java:69) ~[spring-security-web-7.1.2.jar!/:7.1.2]
	at org.springframework.security.web.FilterChainProxy$VirtualFilterChain.doFilter(FilterChainProxy.java:387) ~[spring-security-web-7.1.2.jar!/:7.1.2]
```

## Exception detail

```
21-2026-10-26T10:48:38.537Z  INFO 2171 --- [infeara-website] [           main] com.infeara.site.InfearaApplication      : Started InfearaApplication in 2.603 seconds (process running for 3.056)
22-2026-10-26T10:48:39.573Z  INFO 2171 --- [infeara-website] [nio-8080-exec-1] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring DispatcherServlet 'dispatcherServlet'
23-2026-10-26T10:48:39.573Z  INFO 2171 --- [infeara-website] [nio-8080-exec-1] o.s.web.servlet.DispatcherServlet        : Initializing Servlet 'dispatcherServlet'
24-2026-10-26T10:48:39.574Z  INFO 2171 --- [infeara-website] [nio-8080-exec-1] o.s.web.servlet.DispatcherServlet        : Completed initialization in 0 ms
25:2026-10-26T10:48:43.823Z  INFO 2171 --- [infeara-website] [nio-8080-exec-6] com.infeara.site.cms.CmsSecurityConfig   : CMS sign-in rejected: lockedOut=false type=BadCredentialsException cause=none
26:2026-10-26T10:48:44.384Z  INFO 2171 --- [infeara-website] [nio-8080-exec-6] com.infeara.site.cms.CmsSecurityConfig   : CMS sign-in rejected: lockedOut=false type=BadCredentialsException cause=UsernameNotFoundException
27-2026-10-26T10:48:44.407Z  INFO 2171 --- [infeara-website] [nio-8080-exec-2] com.infeara.site.cms.CmsSecurityConfig   : CMS sign-in rejected: lockedOut=false type=BadCredentialsException cause=none
28-2026-10-26T10:48:44.728Z  INFO 2171 --- [infeara-website] [nio-8080-exec-7] com.infeara.site.cms.CmsSecurityConfig   : CMS sign-in rejected: lockedOut=false type=BadCredentialsException cause=UsernameNotFoundException
29-2026-10-26T10:48:44.994Z  INFO 2171 --- [infeara-website] [nio-8080-exec-6] com.infeara.site.cms.CmsSecurityConfig   : CMS sign-in rejected: lockedOut=false type=BadCredentialsException cause=UsernameNotFoundException
30-2026-10-26T10:48:45.261Z  INFO 2171 --- [infeara-website] [nio-8080-exec-9] com.infeara.site.cms.CmsSecurityConfig   : CMS sign-in rejected: lockedOut=false type=BadCredentialsException cause=UsernameNotFoundException
31-2026-10-26T10:48:45.531Z  INFO 2171 --- [infeara-website] [nio-8080-exec-2] com.infeara.site.cms.CmsSecurityConfig   : CMS sign-in rejected: lockedOut=false type=BadCredentialsException cause=UsernameNotFoundException
32-2026-10-26T10:48:45.796Z  WARN 2171 --- [infeara-website] [nio-8080-exec-5] c.infeara.site.cms.LoginAttemptService   : CMS sign-in locked out for account account:intruder for 15 minute(s) after 5 failure(s)
33-2026-10-26T10:48:45.797Z  INFO 2171 --- [infeara-website] [nio-8080-exec-5] com.infeara.site.cms.CmsSecurityConfig   : CMS sign-in rejected: lockedOut=false type=BadCredentialsException cause=UsernameNotFoundException
34-2026-10-26T10:48:45.809Z ERROR 2171 --- [infeara-website] [nio-8080-exec-7] w.a.UsernamePasswordAuthenticationFilter : An internal error occurred while trying to authenticate the user.
35-
36-org.springframework.security.authentication.InternalAuthenticationServiceException: Too many failed sign-in attempts
37-	at org.springframework.security.authentication.dao.DaoAuthenticationProvider.retrieveUser(DaoAuthenticationProvider.java:121) ~[spring-security-core-7.1.2.jar!/:7.1.2]
38-	at org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider.authenticate(AbstractUserDetailsAuthenticationProvider.java:145) ~[spring-security-core-7.1.2.jar!/:7.1.2]
39-	at org.springframework.security.authentication.ProviderManager.authenticate(ProviderManager.java:185) ~[spring-security-core-7.1.2.jar!/:7.1.2]
40-	at org.springframework.security.authentication.ProviderManager.authenticate(ProviderManager.java:217) ~[spring-security-core-7.1.2.jar!/:7.1.2]
41-	at org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.attemptAuthentication(UsernamePasswordAuthenticationFilter.java:87) ~[spring-security-web-7.1.2.jar!/:7.1.2]
42-	at org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter.doFilter(AbstractAuthenticationProcessingFilter.java:250) ~[spring-security-web-7.1.2.jar!/:7.1.2]
43-	at org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter.doFilter(AbstractAuthenticationProcessingFilter.java:240) ~[spring-security-web-7.1.2.jar!/:7.1.2]
44-	at org.springframework.security.web.FilterChainProxy$VirtualFilterChain.doFilter(FilterChainProxy.java:387) ~[spring-security-web-7.1.2.jar!/:7.1.2]
45-	at com.infeara.site.cms.CmsLoginGuardFilter.doFilterInternal(CmsLoginGuardFilter.java:51) ~[!/:1.0.0]
46-	at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:116) ~[spring-web-7.0.10.jar!/:7.0.10]
47-	at org.springframework.security.web.FilterChainProxy$VirtualFilterChain.doFilter(FilterChainProxy.java:387) ~[spring-security-web-7.1.2.jar!/:7.1.2]
48-	at org.springframework.security.web.authentication.logout.LogoutFilter.doFilter(LogoutFilter.java:110) ~[spring-security-web-7.1.2.jar!/:7.1.2]
49-	at org.springframework.security.web.authentication.logout.LogoutFilter.doFilter(LogoutFilter.java:96) ~[spring-security-web-7.1.2.jar!/:7.1.2]
50-	at org.springframework.security.web.FilterChainProxy$VirtualFilterChain.doFilter(FilterChainProxy.java:387) ~[spring-security-web-7.1.2.jar!/:7.1.2]
```

## CMS gate output

```
CMS sign-in gate against http://localhost:8080

  ok   sign-in page returns 200
  ok   sign-in page is marked noindex
  ok   sign-in form carries a CSRF token
  ok   sign-in form posts to the fixed processing URL
  ok   password field asks for the current password and never echoes one
  ok   sign-in page loads nothing from another origin
  ok   Content-Security-Policy denies other origins and framing
  ok   X-Frame-Options is DENY
  ok   X-Content-Type-Options is nosniff
  ok   Referrer-Policy is no-referrer
  ok   administration area sends X-Robots-Tag noindex
  ok   administration responses are not cacheable
  ok   the workspace redirects an anonymous caller to the sign-in page
  ok   a sign-in post without a CSRF token is refused with 403
  ok   a wrong password is sent back to the form with a generic error
  ok   a failed sign-in does not open the workspace
  ok   one failed attempt does not block the account
  ok   an unknown username is rejected with the same generic error
  FAIL the correct credentials did not reach the workspace (location: http://localhost:8080/cms/login?error)
  FAIL the session cookie is missing, renamed, or not HttpOnly
  FAIL the session cookie does not set SameSite=Lax
  FAIL the workspace did not load for the signed-in account (status 302)
  ok   a sign-out post without a CSRF token is refused
  ok   a GET request cannot sign a session out
  FAIL signing out did not reach the sign-in page (location: )
  ok   the workspace is closed again after signing out
  ok   repeated failures lock the account instead of allowing endless guessing

CMS sign-in gate FAILED (5 of 27 checks failed)
```
