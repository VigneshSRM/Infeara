# CI server diagnostics

Written by the workflow when a gate fails, because job logs are not readable here.

## Response for /cms/login

```
HTTP/1.1 500 
X-Robots-Tag: noindex, nofollow
Set-Cookie: CMS_SESSION=D5B10A52CF0454B24512235864C4D2AD; Path=/; HttpOnly; SameSite=Lax
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
Content-Language: en
Content-Length: 0
Date: Mon, 26 Oct 2026 10:33:59 GMT
Connection: close


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

2026-10-26T10:33:48.122Z  INFO 2366 --- [infeara-website] [           main] com.infeara.site.InfearaApplication      : Starting InfearaApplication v1.0.0 using Java 27.0.1.0.2 with PID 2366 (/home/runner/work/Infeara/Infeara/target/infeara-website-1.0.0.jar started by runner in /home/runner/work/Infeara/Infeara)
2026-10-26T10:33:48.127Z  INFO 2366 --- [infeara-website] [           main] com.infeara.site.InfearaApplication      : No active profile set, falling back to 1 default profile: "default"
2026-10-26T10:33:49.635Z  INFO 2366 --- [infeara-website] [           main] o.s.boot.tomcat.TomcatWebServer          : Tomcat initialized with port 8080 (http)
2026-10-26T10:33:49.649Z  INFO 2366 --- [infeara-website] [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
2026-10-26T10:33:49.650Z  INFO 2366 --- [infeara-website] [           main] o.apache.catalina.core.StandardEngine    : Starting Servlet engine: [Apache Tomcat/11.0.27]
2026-10-26T10:33:49.676Z  INFO 2366 --- [infeara-website] [           main] b.w.c.s.WebApplicationContextInitializer : Root WebApplicationContext: initialization completed in 1463 ms
2026-10-26T10:33:50.369Z  WARN 2366 --- [infeara-website] [           main] c.i.site.cms.CmsUserDetailsService       : CMS sign-in is using a plain secret from the environment; prefer CMS_ADMIN_PASSWORD_HASH so the secret is never readable by the application process
2026-10-26T10:33:50.475Z  INFO 2366 --- [infeara-website] [           main] r$InitializeUserDetailsManagerConfigurer : Global AuthenticationManager configured with UserDetailsService bean with name cmsUserDetailsService
2026-10-26T10:33:50.667Z  INFO 2366 --- [infeara-website] [           main] o.s.b.w.a.WelcomePageHandlerMapping      : Adding welcome page: class path resource [static/index.html]
2026-10-26T10:33:50.946Z  INFO 2366 --- [infeara-website] [           main] o.s.boot.tomcat.TomcatWebServer          : Tomcat started on port 8080 (http) with context path '/'
2026-10-26T10:33:50.959Z  INFO 2366 --- [infeara-website] [           main] com.infeara.site.InfearaApplication      : Started InfearaApplication in 3.429 seconds (process running for 4.078)
2026-10-26T10:33:52.967Z  INFO 2366 --- [infeara-website] [nio-8080-exec-1] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring DispatcherServlet 'dispatcherServlet'
2026-10-26T10:33:52.967Z  INFO 2366 --- [infeara-website] [nio-8080-exec-1] o.s.web.servlet.DispatcherServlet        : Initializing Servlet 'dispatcherServlet'
2026-10-26T10:33:52.968Z  INFO 2366 --- [infeara-website] [nio-8080-exec-1] o.s.web.servlet.DispatcherServlet        : Completed initialization in 1 ms
2026-10-26T10:33:58.431Z ERROR 2366 --- [infeara-website] [nio-8080-exec-3] org.thymeleaf.TemplateEngine             : [THYMELEAF][http-nio-8080-exec-3] Exception processing template "cms/login": Instantiation of new objects and access to static classes or parameters is forbidden in this context (template: "cms/login" - line 63, col 18)

org.thymeleaf.exceptions.TemplateProcessingException: Instantiation of new objects and access to static classes or parameters is forbidden in this context (template: "cms/login" - line 63, col 18)
	at org.thymeleaf.spring6.expression.SPELVariableExpressionEvaluator.obtainComputedSpelExpression(SPELVariableExpressionEvaluator.java:319) ~[thymeleaf-spring6-3.1.5.RELEASE.jar!/:3.1.5.RELEASE]
	at org.thymeleaf.spring6.expression.SPELVariableExpressionEvaluator.evaluate(SPELVariableExpressionEvaluator.java:159) ~[thymeleaf-spring6-3.1.5.RELEASE.jar!/:3.1.5.RELEASE]
	at org.thymeleaf.standard.expression.VariableExpression.executeVariableExpression(VariableExpression.java:166) ~[thymeleaf-3.1.5.RELEASE.jar!/:3.1.5.RELEASE]
	at org.thymeleaf.standard.expression.SimpleExpression.executeSimple(SimpleExpression.java:66) ~[thymeleaf-3.1.5.RELEASE.jar!/:3.1.5.RELEASE]
	at org.thymeleaf.standard.expression.Expression.execute(Expression.java:109) ~[thymeleaf-3.1.5.RELEASE.jar!/:3.1.5.RELEASE]
	at org.thymeleaf.standard.expression.Expression.execute(Expression.java:138) ~[thymeleaf-3.1.5.RELEASE.jar!/:3.1.5.RELEASE]
	at org.thymeleaf.standard.processor.AbstractStandardMultipleAttributeModifierTagProcessor.doProcess(AbstractStandardMultipleAttributeModifierTagProcessor.java:138) ~[thymeleaf-3.1.5.RELEASE.jar!/:3.1.5.RELEASE]
	at org.thymeleaf.processor.element.AbstractAttributeTagProcessor.doProcess(AbstractAttributeTagProcessor.java:74) ~[thymeleaf-3.1.5.RELEASE.jar!/:3.1.5.RELEASE]
	at org.thymeleaf.processor.element.AbstractElementTagProcessor.process(AbstractElementTagProcessor.java:95) ~[thymeleaf-3.1.5.RELEASE.jar!/:3.1.5.RELEASE]
	at org.thymeleaf.util.ProcessorConfigurationUtils$ElementTagProcessorWrapper.process(ProcessorConfigurationUtils.java:633) ~[thymeleaf-3.1.5.RELEASE.jar!/:3.1.5.RELEASE]
	at org.thymeleaf.engine.ProcessorTemplateHandler.handleStandaloneElement(ProcessorTemplateHandler.java:918) ~[thymeleaf-3.1.5.RELEASE.jar!/:3.1.5.RELEASE]
	at org.thymeleaf.engine.StandaloneElementTag.beHandled(StandaloneElementTag.java:228) ~[thymeleaf-3.1.5.RELEASE.jar!/:3.1.5.RELEASE]
	at org.thymeleaf.engine.TemplateModel.process(TemplateModel.java:136) ~[thymeleaf-3.1.5.RELEASE.jar!/:3.1.5.RELEASE]
	at org.thymeleaf.engine.TemplateManager.parseAndProcess(TemplateManager.java:661) ~[thymeleaf-3.1.5.RELEASE.jar!/:3.1.5.RELEASE]
	at org.thymeleaf.TemplateEngine.process(TemplateEngine.java:1103) ~[thymeleaf-3.1.5.RELEASE.jar!/:3.1.5.RELEASE]
	at org.thymeleaf.TemplateEngine.process(TemplateEngine.java:1077) ~[thymeleaf-3.1.5.RELEASE.jar!/:3.1.5.RELEASE]
	at org.thymeleaf.spring6.view.ThymeleafView.renderFragment(ThymeleafView.java:374) ~[thymeleaf-spring6-3.1.5.RELEASE.jar!/:3.1.5.RELEASE]
	at org.thymeleaf.spring6.view.ThymeleafView.render(ThymeleafView.java:193) ~[thymeleaf-spring6-3.1.5.RELEASE.jar!/:3.1.5.RELEASE]
	at org.springframework.web.servlet.DispatcherServlet.render(DispatcherServlet.java:1305) ~[spring-webmvc-7.0.10.jar!/:7.0.10]
	at org.springframework.web.servlet.DispatcherServlet.processDispatchResult(DispatcherServlet.java:1042) ~[spring-webmvc-7.0.10.jar!/:7.0.10]
	at org.springframework.web.servlet.DispatcherServlet.doDispatch(DispatcherServlet.java:980) ~[spring-webmvc-7.0.10.jar!/:7.0.10]
	at org.springframework.web.servlet.DispatcherServlet.doService(DispatcherServlet.java:866) ~[spring-webmvc-7.0.10.jar!/:7.0.10]
	at org.springframework.web.servlet.FrameworkServlet.processRequest(FrameworkServlet.java:1000) ~[spring-webmvc-7.0.10.jar!/:7.0.10]
	at org.springframework.web.servlet.FrameworkServlet.doGet(FrameworkServlet.java:892) ~[spring-webmvc-7.0.10.jar!/:7.0.10]
	at jakarta.servlet.http.HttpServlet.service(HttpServlet.java:622) ~[tomcat-embed-core-11.0.27.jar!/:6.1]
	at org.springframework.web.servlet.FrameworkServlet.service(FrameworkServlet.java:874) ~[spring-webmvc-7.0.10.jar!/:7.0.10]
	at jakarta.servlet.http.HttpServlet.service(HttpServlet.java:710) ~[tomcat-embed-core-11.0.27.jar!/:6.1]
	at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:132) ~[tomcat-embed-core-11.0.27.jar!/:11.0.27]
	at org.apache.tomcat.websocket.server.WsFilter.doFilter(WsFilter.java:59) ~[tomcat-embed-websocket-11.0.27.jar!/:11.0.27]
	at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:111) ~[tomcat-embed-core-11.0.27.jar!/:11.0.27]
	at com.infeara.site.seo.SeoRequestFilter.doFilterInternal(SeoRequestFilter.java:57) ~[!/:1.0.0]
	at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:116) ~[spring-web-7.0.10.jar!/:7.0.10]
	at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:111) ~[tomcat-embed-core-11.0.27.jar!/:11.0.27]
```

## Exception detail

```
21-2026-10-26T10:33:50.959Z  INFO 2366 --- [infeara-website] [           main] com.infeara.site.InfearaApplication      : Started InfearaApplication in 3.429 seconds (process running for 4.078)
22-2026-10-26T10:33:52.967Z  INFO 2366 --- [infeara-website] [nio-8080-exec-1] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring DispatcherServlet 'dispatcherServlet'
23-2026-10-26T10:33:52.967Z  INFO 2366 --- [infeara-website] [nio-8080-exec-1] o.s.web.servlet.DispatcherServlet        : Initializing Servlet 'dispatcherServlet'
24-2026-10-26T10:33:52.968Z  INFO 2366 --- [infeara-website] [nio-8080-exec-1] o.s.web.servlet.DispatcherServlet        : Completed initialization in 1 ms
25:2026-10-26T10:33:58.431Z ERROR 2366 --- [infeara-website] [nio-8080-exec-3] org.thymeleaf.TemplateEngine             : [THYMELEAF][http-nio-8080-exec-3] Exception processing template "cms/login": Instantiation of new objects and access to static classes or parameters is forbidden in this context (template: "cms/login" - line 63, col 18)
26-
27:org.thymeleaf.exceptions.TemplateProcessingException: Instantiation of new objects and access to static classes or parameters is forbidden in this context (template: "cms/login" - line 63, col 18)
28-	at org.thymeleaf.spring6.expression.SPELVariableExpressionEvaluator.obtainComputedSpelExpression(SPELVariableExpressionEvaluator.java:319) ~[thymeleaf-spring6-3.1.5.RELEASE.jar!/:3.1.5.RELEASE]
29-	at org.thymeleaf.spring6.expression.SPELVariableExpressionEvaluator.evaluate(SPELVariableExpressionEvaluator.java:159) ~[thymeleaf-spring6-3.1.5.RELEASE.jar!/:3.1.5.RELEASE]
30-	at org.thymeleaf.standard.expression.VariableExpression.executeVariableExpression(VariableExpression.java:166) ~[thymeleaf-3.1.5.RELEASE.jar!/:3.1.5.RELEASE]
31-	at org.thymeleaf.standard.expression.SimpleExpression.executeSimple(SimpleExpression.java:66) ~[thymeleaf-3.1.5.RELEASE.jar!/:3.1.5.RELEASE]
32-	at org.thymeleaf.standard.expression.Expression.execute(Expression.java:109) ~[thymeleaf-3.1.5.RELEASE.jar!/:3.1.5.RELEASE]
33-	at org.thymeleaf.standard.expression.Expression.execute(Expression.java:138) ~[thymeleaf-3.1.5.RELEASE.jar!/:3.1.5.RELEASE]
34-	at org.thymeleaf.standard.processor.AbstractStandardMultipleAttributeModifierTagProcessor.doProcess(AbstractStandardMultipleAttributeModifierTagProcessor.java:138) ~[thymeleaf-3.1.5.RELEASE.jar!/:3.1.5.RELEASE]
35-	at org.thymeleaf.processor.element.AbstractAttributeTagProcessor.doProcess(AbstractAttributeTagProcessor.java:74) ~[thymeleaf-3.1.5.RELEASE.jar!/:3.1.5.RELEASE]
36-	at org.thymeleaf.processor.element.AbstractElementTagProcessor.process(AbstractElementTagProcessor.java:95) ~[thymeleaf-3.1.5.RELEASE.jar!/:3.1.5.RELEASE]
37-	at org.thymeleaf.util.ProcessorConfigurationUtils$ElementTagProcessorWrapper.process(ProcessorConfigurationUtils.java:633) ~[thymeleaf-3.1.5.RELEASE.jar!/:3.1.5.RELEASE]
38-	at org.thymeleaf.engine.ProcessorTemplateHandler.handleStandaloneElement(ProcessorTemplateHandler.java:918) ~[thymeleaf-3.1.5.RELEASE.jar!/:3.1.5.RELEASE]
39-	at org.thymeleaf.engine.StandaloneElementTag.beHandled(StandaloneElementTag.java:228) ~[thymeleaf-3.1.5.RELEASE.jar!/:3.1.5.RELEASE]
40-	at org.thymeleaf.engine.TemplateModel.process(TemplateModel.java:136) ~[thymeleaf-3.1.5.RELEASE.jar!/:3.1.5.RELEASE]
41-	at org.thymeleaf.engine.TemplateManager.parseAndProcess(TemplateManager.java:661) ~[thymeleaf-3.1.5.RELEASE.jar!/:3.1.5.RELEASE]
42-	at org.thymeleaf.TemplateEngine.process(TemplateEngine.java:1103) ~[thymeleaf-3.1.5.RELEASE.jar!/:3.1.5.RELEASE]
43-	at org.thymeleaf.TemplateEngine.process(TemplateEngine.java:1077) ~[thymeleaf-3.1.5.RELEASE.jar!/:3.1.5.RELEASE]
44-	at org.thymeleaf.spring6.view.ThymeleafView.renderFragment(ThymeleafView.java:374) ~[thymeleaf-spring6-3.1.5.RELEASE.jar!/:3.1.5.RELEASE]
45-	at org.thymeleaf.spring6.view.ThymeleafView.render(ThymeleafView.java:193) ~[thymeleaf-spring6-3.1.5.RELEASE.jar!/:3.1.5.RELEASE]
46-	at org.springframework.web.servlet.DispatcherServlet.render(DispatcherServlet.java:1305) ~[spring-webmvc-7.0.10.jar!/:7.0.10]
47-	at org.springframework.web.servlet.DispatcherServlet.processDispatchResult(DispatcherServlet.java:1042) ~[spring-webmvc-7.0.10.jar!/:7.0.10]
48-	at org.springframework.web.servlet.DispatcherServlet.doDispatch(DispatcherServlet.java:980) ~[spring-webmvc-7.0.10.jar!/:7.0.10]
49-	at org.springframework.web.servlet.DispatcherServlet.doService(DispatcherServlet.java:866) ~[spring-webmvc-7.0.10.jar!/:7.0.10]
50-	at org.springframework.web.servlet.FrameworkServlet.processRequest(FrameworkServlet.java:1000) ~[spring-webmvc-7.0.10.jar!/:7.0.10]
51-	at org.springframework.web.servlet.FrameworkServlet.doGet(FrameworkServlet.java:892) ~[spring-webmvc-7.0.10.jar!/:7.0.10]
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
