# CI server diagnostics

Written by the workflow when a gate fails, because job logs are not readable here.

## Response for /cms/login

```
HTTP/1.1 200 
X-Robots-Tag: noindex, nofollow
Set-Cookie: CMS_SESSION=FEFD10AA8CB9A8DC3E776674D1082718; Path=/; HttpOnly; SameSite=Lax
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
Date: Mon, 26 Oct 2026 10:39:00 GMT

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

2026-10-26T10:38:49.677Z  INFO 3057 --- [infeara-website] [           main] com.infeara.site.InfearaApplication      : Starting InfearaApplication v1.0.0 using Java 27.0.1.0.2 with PID 3057 (/home/runner/work/Infeara/Infeara/target/infeara-website-1.0.0.jar started by runner in /home/runner/work/Infeara/Infeara)
2026-10-26T10:38:49.687Z  INFO 3057 --- [infeara-website] [           main] com.infeara.site.InfearaApplication      : No active profile set, falling back to 1 default profile: "default"
2026-10-26T10:38:51.036Z  INFO 3057 --- [infeara-website] [           main] o.s.boot.tomcat.TomcatWebServer          : Tomcat initialized with port 8080 (http)
2026-10-26T10:38:51.067Z  INFO 3057 --- [infeara-website] [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
2026-10-26T10:38:51.067Z  INFO 3057 --- [infeara-website] [           main] o.apache.catalina.core.StandardEngine    : Starting Servlet engine: [Apache Tomcat/11.0.27]
2026-10-26T10:38:51.095Z  INFO 3057 --- [infeara-website] [           main] b.w.c.s.WebApplicationContextInitializer : Root WebApplicationContext: initialization completed in 1297 ms
2026-10-26T10:38:51.822Z  WARN 3057 --- [infeara-website] [           main] c.i.site.cms.CmsUserDetailsService       : CMS sign-in is using a plain secret from the environment; prefer CMS_ADMIN_PASSWORD_HASH so the secret is never readable by the application process
2026-10-26T10:38:51.909Z  INFO 3057 --- [infeara-website] [           main] r$InitializeUserDetailsManagerConfigurer : Global AuthenticationManager configured with UserDetailsService bean with name cmsUserDetailsService
2026-10-26T10:38:52.135Z  INFO 3057 --- [infeara-website] [           main] o.s.b.w.a.WelcomePageHandlerMapping      : Adding welcome page: class path resource [static/index.html]
2026-10-26T10:38:52.402Z  INFO 3057 --- [infeara-website] [           main] o.s.boot.tomcat.TomcatWebServer          : Tomcat started on port 8080 (http) with context path '/'
2026-10-26T10:38:52.414Z  INFO 3057 --- [infeara-website] [           main] com.infeara.site.InfearaApplication      : Started InfearaApplication in 3.27 seconds (process running for 3.851)
2026-10-26T10:38:52.648Z  INFO 3057 --- [infeara-website] [nio-8080-exec-1] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring DispatcherServlet 'dispatcherServlet'
2026-10-26T10:38:52.648Z  INFO 3057 --- [infeara-website] [nio-8080-exec-1] o.s.web.servlet.DispatcherServlet        : Initializing Servlet 'dispatcherServlet'
2026-10-26T10:38:52.649Z  INFO 3057 --- [infeara-website] [nio-8080-exec-1] o.s.web.servlet.DispatcherServlet        : Completed initialization in 1 ms
2026-10-26T10:39:00.262Z  WARN 3057 --- [infeara-website] [nio-8080-exec-1] c.infeara.site.cms.LoginAttemptService   : CMS sign-in locked out for account account:intruder for 15 minute(s) after 5 failure(s)
2026-10-26T10:39:00.277Z ERROR 3057 --- [infeara-website] [nio-8080-exec-5] w.a.UsernamePasswordAuthenticationFilter : An internal error occurred while trying to authenticate the user.

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
	at org.springframework.security.web.context.request.async.WebAsyncManagerIntegrationFilter.doFilterInternal(WebAsyncManagerIntegrationFilter.java:62) ~[spring-security-web-7.1.2.jar!/:7.1.2]
	at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:116) ~[spring-web-7.0.10.jar!/:7.0.10]
	at org.springframework.security.web.FilterChainProxy$VirtualFilterChain.doFilter(FilterChainProxy.java:387) ~[spring-security-web-7.1.2.jar!/:7.1.2]
	at org.springframework.security.web.session.DisableEncodeUrlFilter.doFilterInternal(DisableEncodeUrlFilter.java:42) ~[spring-security-web-7.1.2.jar!/:7.1.2]
	at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:116) ~[spring-web-7.0.10.jar!/:7.0.10]
	at org.springframework.security.web.FilterChainProxy$VirtualFilterChain.doFilter(FilterChainProxy.java:387) ~[spring-security-web-7.1.2.jar!/:7.1.2]
	at org.springframework.security.web.FilterChainProxy.doFilterInternal(FilterChainProxy.java:237) ~[spring-security-web-7.1.2.jar!/:7.1.2]
	at org.springframework.security.web.FilterChainProxy.doFilter(FilterChainProxy.java:195) ~[spring-security-web-7.1.2.jar!/:7.1.2]
```

## Exception detail

```
24-2026-10-26T10:38:52.649Z  INFO 3057 --- [infeara-website] [nio-8080-exec-1] o.s.web.servlet.DispatcherServlet        : Completed initialization in 1 ms
25-2026-10-26T10:39:00.262Z  WARN 3057 --- [infeara-website] [nio-8080-exec-1] c.infeara.site.cms.LoginAttemptService   : CMS sign-in locked out for account account:intruder for 15 minute(s) after 5 failure(s)
26-2026-10-26T10:39:00.277Z ERROR 3057 --- [infeara-website] [nio-8080-exec-5] w.a.UsernamePasswordAuthenticationFilter : An internal error occurred while trying to authenticate the user.
27-
28:org.springframework.security.authentication.InternalAuthenticationServiceException: Too many failed sign-in attempts
29-	at org.springframework.security.authentication.dao.DaoAuthenticationProvider.retrieveUser(DaoAuthenticationProvider.java:121) ~[spring-security-core-7.1.2.jar!/:7.1.2]
30-	at org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider.authenticate(AbstractUserDetailsAuthenticationProvider.java:145) ~[spring-security-core-7.1.2.jar!/:7.1.2]
31-	at org.springframework.security.authentication.ProviderManager.authenticate(ProviderManager.java:185) ~[spring-security-core-7.1.2.jar!/:7.1.2]
32-	at org.springframework.security.authentication.ProviderManager.authenticate(ProviderManager.java:217) ~[spring-security-core-7.1.2.jar!/:7.1.2]
33-	at org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.attemptAuthentication(UsernamePasswordAuthenticationFilter.java:87) ~[spring-security-web-7.1.2.jar!/:7.1.2]
34-	at org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter.doFilter(AbstractAuthenticationProcessingFilter.java:250) ~[spring-security-web-7.1.2.jar!/:7.1.2]
35-	at org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter.doFilter(AbstractAuthenticationProcessingFilter.java:240) ~[spring-security-web-7.1.2.jar!/:7.1.2]
36-	at org.springframework.security.web.FilterChainProxy$VirtualFilterChain.doFilter(FilterChainProxy.java:387) ~[spring-security-web-7.1.2.jar!/:7.1.2]
37-	at com.infeara.site.cms.CmsLoginGuardFilter.doFilterInternal(CmsLoginGuardFilter.java:51) ~[!/:1.0.0]
38-	at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:116) ~[spring-web-7.0.10.jar!/:7.0.10]
39-	at org.springframework.security.web.FilterChainProxy$VirtualFilterChain.doFilter(FilterChainProxy.java:387) ~[spring-security-web-7.1.2.jar!/:7.1.2]
40-	at org.springframework.security.web.authentication.logout.LogoutFilter.doFilter(LogoutFilter.java:110) ~[spring-security-web-7.1.2.jar!/:7.1.2]
41-	at org.springframework.security.web.authentication.logout.LogoutFilter.doFilter(LogoutFilter.java:96) ~[spring-security-web-7.1.2.jar!/:7.1.2]
42-	at org.springframework.security.web.FilterChainProxy$VirtualFilterChain.doFilter(FilterChainProxy.java:387) ~[spring-security-web-7.1.2.jar!/:7.1.2]
43-	at org.springframework.security.web.csrf.CsrfFilter.doFilterInternal(CsrfFilter.java:135) ~[spring-security-web-7.1.2.jar!/:7.1.2]
44-	at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:116) ~[spring-web-7.0.10.jar!/:7.0.10]
45-	at org.springframework.security.web.FilterChainProxy$VirtualFilterChain.doFilter(FilterChainProxy.java:387) ~[spring-security-web-7.1.2.jar!/:7.1.2]
46-	at org.springframework.security.web.header.HeaderWriterFilter.doHeadersAfter(HeaderWriterFilter.java:91) ~[spring-security-web-7.1.2.jar!/:7.1.2]
47-	at org.springframework.security.web.header.HeaderWriterFilter.doFilterInternal(HeaderWriterFilter.java:76) ~[spring-security-web-7.1.2.jar!/:7.1.2]
48-	at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:116) ~[spring-web-7.0.10.jar!/:7.0.10]
49-	at org.springframework.security.web.FilterChainProxy$VirtualFilterChain.doFilter(FilterChainProxy.java:387) ~[spring-security-web-7.1.2.jar!/:7.1.2]
50-	at org.springframework.security.web.context.SecurityContextHolderFilter.doFilter(SecurityContextHolderFilter.java:82) ~[spring-security-web-7.1.2.jar!/:7.1.2]
51-	at org.springframework.security.web.context.SecurityContextHolderFilter.doFilter(SecurityContextHolderFilter.java:69) ~[spring-security-web-7.1.2.jar!/:7.1.2]
52-	at org.springframework.security.web.FilterChainProxy$VirtualFilterChain.doFilter(FilterChainProxy.java:387) ~[spring-security-web-7.1.2.jar!/:7.1.2]
--
90-	at org.apache.tomcat.util.threads.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:947) ~[tomcat-embed-core-11.0.27.jar!/:11.0.27]
91-	at org.apache.tomcat.util.threads.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:481) ~[tomcat-embed-core-11.0.27.jar!/:11.0.27]
92-	at org.apache.tomcat.util.threads.TaskThread$WrappingRunnable.run(TaskThread.java:74) ~[tomcat-embed-core-11.0.27.jar!/:11.0.27]
93-	at java.base/java.lang.Thread.run(Thread.java:1527) ~[na:na]
94:Caused by: org.springframework.security.authentication.LockedException: Too many failed sign-in attempts
95-	at com.infeara.site.cms.CmsUserDetailsService.loadUserByUsername(CmsUserDetailsService.java:47) ~[!/:1.0.0]
96-	at org.springframework.security.authentication.dao.DaoAuthenticationProvider.retrieveUser(DaoAuthenticationProvider.java:106) ~[spring-security-core-7.1.2.jar!/:7.1.2]
97-	... 64 common frames omitted
98-
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
  ok   an unknown username is rejected with the same generic error
  ok   the correct credentials are sent to the workspace
  ok   the session cookie is named CMS_SESSION and is HttpOnly
  ok   the session cookie is SameSite=Lax
  ok   the workspace loads for the signed-in account and offers a sign-out form
  ok   a sign-out post without a CSRF token is refused
  ok   a GET request cannot sign a session out
  ok   a signed-out session is returned to the sign-in page
  ok   the workspace is closed again after signing out
  FAIL repeated failures never locked the account (last location: http://localhost:8080/cms/login?error)
  FAIL the lockout blocked a legitimate sign-in (location: http://localhost:8080/cms/login?error)

CMS sign-in gate FAILED (2 of 27 checks failed)
```
