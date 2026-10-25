# SEO implementation standard — Infeara website

This document records how the site implements *Website SEO Standards & Implementation Guide*
(version 1.0, 25 October 2026): what was wrong before, what changed, how each rule is enforced,
and what still needs a human or hosting-level decision.

Rule labels used below follow the standard: **MUST** is release-blocking, **SHOULD** is a strong
standard, **TARGET** is an internal engineering goal rather than a Google requirement.

---

## 1. Where the implementation lives

| Concern | Location |
| --- | --- |
| Canonical origin, indexability switch, robots directive | `src/main/java/com/infeara/site/seo/SeoProperties.java` |
| Title/description length and slug helpers | `src/main/java/com/infeara/site/seo/SeoText.java` |
| Copy length and duplicate audit of the rendered site | `preview/report.py` |
| JSON-LD (schema.org) construction and escaping | `src/main/java/com/infeara/site/seo/JsonLd.java` |
| Per-page canonical, social image, breadcrumb/service/article graphs | `src/main/java/com/infeara/site/seo/SeoService.java` |
| URL variant redirects, `X-Robots-Tag` headers | `src/main/java/com/infeara/site/seo/SeoRequestFilter.java` |
| `/sitemap.xml` generated from published content | `src/main/java/com/infeara/site/seo/SitemapController.java` |
| `/robots.txt` | `src/main/java/com/infeara/site/seo/RobotsController.java` |
| Static asset caching | `src/main/java/com/infeara/site/web/WebConfig.java` |
| Authored per-page copy (title, description, body, FAQ) | `src/main/resources/content/page-copy*.txt` |
| Copy loading, validation and lookup | `src/main/java/com/infeara/site/content/PageCopyService.java`, `PageCopy.java` |
| Page metadata wiring and legacy redirects | `src/main/java/com/infeara/site/web/PageController.java` |
| Reusable body sections rendered from that copy | `src/main/resources/templates/fragments/copy.html` |
| Head markup shared by every page | `src/main/resources/templates/fragments/site.html` |
| Release gate | `scripts/seo-smoke-test.sh`, `.github/workflows/seo.yml` |

## 2. Problems found in the previous build, and the fix

| # | Problem (rule) | Impact | Fix |
| --- | --- | --- | --- |
| 1 | No `/robots.txt` and no `/sitemap.xml` anywhere in the app. | Crawlers had no sitemap, no crawler guidance, and no way to discover the 100+ pages except by following links. | `RobotsController` and `SitemapController` now serve both, generated from published content only. |
| 2 | `SITE_BASE_URL` defaulted to `http://localhost:8080` with no guard, and nothing marked non-production deployments as non-indexable. | A staging or preview deployment using the default configuration would publish `localhost` canonicals and could be indexed. | Canonicals always come from configuration and never from the request host; indexing is allowed only when `seo.base-url` is HTTPS or `SITE_INDEXABLE=true` is set. Non-indexable deployments send `X-Robots-Tag: noindex, nofollow` and serve `Disallow: /`. |
| 3 | The head fragment had no Open Graph or Twitter metadata, no robots directive, no structured data, and an absolute canonical only (no social image). | Shared links rendered without a title, description or image; no breadcrumb, service or article markup was available. | The shared head fragment now emits robots, Open Graph, Twitter card, canonical and JSON-LD for every route. |
| 4 | Trailing-slash and `index.html` URL variants existed. `/services/{slug}/` was handled by extra controller mappings; `/index.html` and other `.html` files were still served from `src/main/resources/static/`. | Two URLs for one page, with the `.html` copies carrying no canonical, no description and no robots directive — a duplicate-content signal and a second, unmanaged copy of the site. | `SeoRequestFilter` answers `/path/` and `/path/index.html` with a single 301 to `/path`; every legacy `.html` route is a one-hop 301 in `PageController`. |
| 5 | Blog and field-note articles were served at both `/blogs/{slug}` and `/insights/{slug}` from the same `insights` rows. | Every article existed twice with different titles ("… | Infeara Blog" vs "… | Infeara Field Notes"). | `/insights/{slug}` now 301s to `/blogs/{slug}`, and internal links point at the canonical blog URL. |
| 6 | Unsupported page claims: the home page said "Technology Partner for What's Next" with a description that did not mention automation despite the navigation doing so. | Generic snippet copy, not a compliance failure. | Titles and descriptions are now derived from each page's own content and held to the 50–60 / 140–160 character editorial targets by `SeoText`. |
| 7 | Header logo shipped a 2172×724 / 467 KB PNG at a 230 px display size, never actually referenced the 1200×436 `infeara-logo.png`, the 2 MB careers PNGs were served at full size, and several `<img>` elements had no `width`/`height`. | LCP and CLS risk on every page (CLS > 0.1 and LCP > 2.5 s risk from image delivery alone). | Added `assets/brand/infeara-logo-460.{png,webp}` (28 KB / 12 KB) used through `<picture>`; careers illustrations now use responsive WebP `srcset`; every content image declares intrinsic dimensions; the hero/service/industry LCP image is preloaded server-side through `preloadImage` and marked `fetchpriority="high"`. |
| 8 | Google Fonts CSS was render-blocking, and `Inter` was referenced as a fallback even though it was never loaded. | Render-blocking third party request; unnecessary font fallback. | Font CSS preloaded and loaded non-blocking (`media="print"` + `onload`), with a `<noscript>` fallback; font stacks trimmed to fonts that are actually loaded. |
| 9 | `/favicon.png` is 128×99 — not square, so Google's favicon guidelines are not met. | Missing or ignored favicon in search results. | Added a square 192×192 `assets/favicon-192.png` and declared `sizes="192x192"`. |
| 10 | No compression and no explicit static-asset caching policy. | Larger transfers and a slower TTFB/FCP than necessary. | `server.compression.*` enabled for HTML/CSS/JS/JSON/XML/SVG; `WebConfig` sets long-lived immutable caching for `/assets`, `/css` and `/js` (versions are already part of the URLs). |
| 11 | No automated technical SEO verification. | Regressions (duplicate titles, missing canonicals, a broken sitemap entry) could reach production unnoticed. | `scripts/seo-smoke-test.sh` checks status codes, canonical/robots/title/description uniqueness, JSON-LD presence, one-hop redirects, `robots.txt` and sitemap hygiene; `.github/workflows/seo.yml` runs it against a started instance on every push and pull request. |

### 2.1 Runtime bugs found by the release gate

The gate runs against a live instance (CI builds the jar, starts it, then crawls it), which surfaced
two defects that no static review could catch. Both are fixed:

| Symptom | Cause | Fix |
| --- | --- | --- |
| `/contact` returned a response truncated after ~35 KB; the enquiry form never rendered. | The two free-text `<option>` elements in the service picker had no `value`; Thymeleaf aborts `th:field` selects in that case. | Explicit `value` attributes on both options. |
| Also on `/contact`: `th:field` could not read the bound object. | `ConsultationRequest` is a record, so it only exposes `fullName()`, while Thymeleaf resolves values through Spring's bean wrapper (`getFullName()` / `isConsent()`). | Added read accessors; the record stays immutable, so submissions still bind through the canonical constructor. |

## 3. What each page now returns

Every rendered page (Thymeleaf templates in `src/main/resources/templates/`) gets:

- `<title>` — authored per page and unique; capability, industry and static-page titles come from the copy library already inside the 50–62 character range, and long article/newsletter titles fall back to the shorter `| Infeara` suffix (`SeoText.titleWithSuffix`).
- `<meta name="description">` — authored per page and unique, 140–160 characters, describing what is actually on that page.
- `<link rel="canonical">` — absolute, HTTPS in production, self-referencing, query strings and fragments removed.
- `<meta name="robots">` — `index,follow` on production, `noindex,nofollow` on any deployment that is not cleared for indexing.
- Open Graph (`og:site_name`, `og:type`, `og:title`, `og:description`, `og:url`, `og:image` 1200×630, `article:published_time` for articles) and a Twitter `summary_large_image` card.
- JSON-LD `@graph` with `BreadcrumbList` matching the visible breadcrumb trail, plus `Organization`/`WebSite` on the home and about pages, `Service` on service pages and `BlogPosting` on articles.
- Exactly one `<h1>` per page (verified across all 19 templates).

`Noindex` is never used to hide thin content: consent, legal and careers pages are normal indexable pages.

### 3.1 Page copy library

Every indexable page draws its search-facing metadata and its body copy from
`src/main/resources/content/`:

| File | Covers |
| --- | --- |
| `page-copy-services.txt` | the 13 static routes and the 9 service areas |
| `page-copy-capabilities.txt` | the 54 capability pages (`parent/slug` keys) |
| `page-copy-industries.txt` | the 24 industry pages |

Each entry supplies `title`, `description`, `intro`, `covers`, `signs`, `deliverables` and two or
more `faq` pairs. `PageCopyService` loads the files at startup and fails the application if any
route in `WebsiteContent` has no copy, if a title or description is empty, longer than the search
snippet limit, or a duplicate of another page. That check is what keeps rule 4.1 (unique metadata)
and rule 5 (no boilerplate body copy) true over time rather than only at launch.

`signs`, `deliverables` and `faq` render through `templates/fragments/copy.html`. The answers state
scope limits, access requirements and trade-offs instead of a fixed product FAQ set, so they are
plain visible content and are deliberately **not** marked up as `FAQPage` structured data (rule 7).
`primaryKeyword` stays an internal mapping note and is never emitted as a meta keyword tag.

## 4. URL policy

| Rule | Implementation |
| --- | --- |
| No trailing slash on any canonical URL | `/path/` → 301 `/path` (`SeoRequestFilter`) |
| No `index.html` in public URLs | `/path/index.html` → 301 `/path` (`SeoRequestFilter`) |
| Legacy files | `/index.html`, `/services.html`, `/industries.html`, `/careers.html`, `/about.html`, `/clients.html`, `/insights.html`, `/blogs.html`, `/newsletters.html`, `/contact.html`, `/privacy.html`, `/terms.html`, `/partners.html`, `/insight-{slug}.html`, `/blog-{slug}.html`, `/newsletter-{slug}.html` → one-hop 301 |
| Retired page | `/partners` and `/partners.html` → 301 `/clients` |
| Duplicate article view | `/insights/{slug}` → 301 `/blogs/{slug}` |
| Lowercase, hyphenated, descriptive slugs | Content slugs come from `WebsiteContent` / `insights.slug`; `SeoText.slug` is the shared derivation for generated pages |
| Unknown URLs | Real 404 from Spring MVC (`ResponseStatusException` for unknown service/industry/article slugs); no SPA-style 200 fallback |

Verified: the 9 service areas, 54 capability pages and 24 industry pages generated from
`WebsiteContent` match the previously published URL set exactly, so no URL migration is needed.

## 5. Configuration

| Variable | Purpose |
| --- | --- |
| `SITE_BASE_URL` → `seo.base-url` | Canonical origin. **Production is `https://www.infeara.com`** and is the default in `application.properties` and the `Dockerfile`; local work sets `http://localhost:8080`. No trailing slash. Used for canonical links, Open Graph URLs, sitemap entries, JSON-LD `@id`/`url` values and the `Sitemap:` line in `robots.txt`. |
| `SITE_INDEXABLE` → `seo.indexable` | `true` allows indexing, `false` forces `noindex` and `Disallow: /`. Empty (default) means "indexable only when `SITE_BASE_URL` is HTTPS". Set `true` explicitly in production so the decision does not depend on the protocol of the base URL. |

### One hostname only

The canonical origin is `https://www.infeara.com`, so `infeara.com` must not be indexed as a
second copy of the site. `SeoRequestFilter` answers a request whose `Host` is exactly the bare
domain with a one-hop `301` to the same path on `www`, preserving the query string. Any other
hostname — `localhost`, a preview URL, an internal service name — is served normally, so a test
or staging deployment is never bounced to production. HTTPS termination and the `http → https`
upgrade stay at the hosting layer, where the certificate is.

CI verifies all of this on a real instance (`production-url-check` job): canonical on `/`,
`og:url`, every `<loc>` in `sitemap.xml`, the `robots.txt` sitemap line, the bare-domain `301`
and the `www` host returning `200`.

## 5.1 Running the application

The site is a Spring Boot 4.1.2 application on Java 27 and needs nothing beyond a JDK and Maven:

```bash
mvn -DskipTests package
SITE_BASE_URL=https://www.example.com SITE_INDEXABLE=true java -jar target/*.jar
```

`SITE_BASE_URL` is the only source of the canonical origin — the `Host` header is never trusted —
and `SITE_INDEXABLE` must be set only on the production deployment. Without it the app still
serves every page, but sends `Disallow: /` and an `X-Robots-Tag: noindex` so a staging copy
cannot be indexed.

Startup also validates the copy library: `PageCopyService` fails the application if any of the
103 routes is missing copy, or if a title or description is duplicated or outside the search
snippet limits. A clean "Started InfearaApplication" line therefore also means rule 4.1 and the
content-quality rules passed for the whole site, not just for the page that gets tested.

Verified on the CI runner (`gh api repos/VigneshSRM/Infeara/check-runs/<job-id>/annotations`):

```
openjdk version "27.0.1.0.2" 2026-10-21          Spring-Boot-Version: 4.1.2
Tomcat started on port 8080 (http) with context path '/'
Started InfearaApplication in 2.448 seconds (process running for 3.025)
GET /services/network-engineering/wi-fi-infrastructure
  <title>Wi-Fi Infrastructure Design & Deployment | Network Engineering</title>
  2 scope/deliverable columns, 1 question list, 1 H1
```

## 5.2 Reviewing the running site

`docs/runtime-snapshot/` holds the response bodies CI recorded from the running application, one
file per sitemap URL, plus `robots.txt` and `sitemap.xml`. To browse them:

```bash
python3 scripts/serve-runtime-snapshot.py        # http://localhost:8080
```

CSS, JS and images are served from `src/main/resources/static`, so the pages look and behave as
the server delivered them. This is a review aid for environments without a JDK - the application
itself still runs from `mvn -DskipTests package && java -jar target/*.jar`.

## 6. Release gate

```sh
mvn spring-boot:run        # in one shell
scripts/seo-smoke-test.sh  # in another shell, against http://localhost:8080
```

Or, without a local JDK: `docker build -t infeara-website . && docker run --rm -p 8080:8080 infeara-website`,
then run the script against `http://localhost:8080`. CI (`.github/workflows/seo.yml`) builds the jar,
starts it, runs the same gate, and uploads the runnable jar as a workflow artifact.

The script fails the release when:

- `robots.txt` is missing, disallows the whole site, or does not declare a sitemap;
- `sitemap.xml` is missing or contains a URL that is not `200`;
- a URL variant (`/path/`, `/index.html`, `/services.html`) is not a one-hop permanent redirect;
- an unknown URL does not return `404`/`410`;
- a checked page has no canonical, a canonical that differs from the expected absolute URL, a `noindex` directive, no description, no `og:title`, a duplicate title/description, or an over-long title;
- JSON-LD is not valid JSON, or a page that visibly renders a breadcrumb trail has no `BreadcrumbList` markup.

## 7. Performance work done and targets

Targets from the standard: LCP ≤ 2.5 s, INP ≤ 200 ms, CLS ≤ 0.1 at p75; Lighthouse performance 90+ on representative mobile pages; initial transfer around 1.5–2.0 MB; keep JavaScript small.

Done in this pass:

- Header logo 467 KB → 28 KB PNG / 12 KB WebP, served at its display size, with explicit dimensions.
- Careers illustrations 2.7 MB + 1.6 MB → 110 KB / 60 KB WebP at the sizes actually displayed, PNG fallback retained.
- All 24 photographic assets re-encoded progressively (≈215 KB saved in total, no visible quality change).
- LCP image preloaded for service, capability, industry and careers pages; `fetchpriority="high"` on the hero images; below-the-fold images keep `loading="lazy"`.
- Every `<img>` reserves layout space with `width`/`height`, so late images cannot shift content.
- Font CSS no longer render-blocking; fallback `Inter` reference removed.
- Text responses compressed (gzip/Brotli-capable clients), static assets cached with versioned URLs.

Still open (measurement, not code): run PageSpeed Insights mobile and desktop on the home page, a service page, an industry page and an article; record LCP element, INP field data and CLS. `.careers/careers-hero.png` (2 MB) is not referenced by any template and can be deleted or converted before it is ever used.

## 8. Remaining recommendations (need content, hosting or business decisions)

1. **Self-host the two fonts.** `fonts.gstatic.com` was unreachable from the build environment, so the Google Fonts delivery was only made non-blocking. Self-hosting subsetted WOFF2 files removes a third-party connection and the remaining flash of unstyled text.
2. **Replace the generated social card.** `assets/social/infeara-social-card.png` is a 1200×630 lockup rendered from the existing brand files. Replace it with an approved campaign image if one exists.
3. **Preferred host and HTTPS redirects.** `SITE_BASE_URL` fixes the canonical origin, but the `http://` → `https://`, `www`/non-`www` and alternate-host redirects belong at the proxy/CDN in front of the app. Test all variants and confirm one hop each.
4. **Delete or archive the legacy static mirror.** `src/main/resources/static/*.html` and the nested `index.html` files are unreachable (controller mappings and the SEO filter take precedence) but they are a second, unmanaged copy of the marketing site with no canonical, description or robots directive. Removing them removes the risk of them being served by a static host or a misconfigured resource handler.
5. **Article metadata from the CMS.** `insights` rows currently expose only a display date string (`25 Oct 2026`), mapped to ISO-8601 for structured data. Add real `published_at`/`updated_at` timestamps plus an author profile if articles will carry named authors — `Article` markup is stronger with a verified author.
6. **Per-page social images.** All pages share one Open Graph image. Service and industry pages would benefit from their own hero image (`og:image`) — the plumbing (`ogImage` model attribute) already supports it.
7. **Supabase-backed pages and caching.** `/blogs`, `/insights` listing and `/newsletters` call Supabase on every request. Add a short-lived cache (or an ETag) once traffic grows; do not cache `/contact` POST responses.
8. **Analytics and consent.** No analytics or advertising scripts are loaded. When one is added, gate it behind consent and record conversion events for the consultation form (the standard's analytics rule).
9. **Field data.** Core Web Vitals field data only becomes available after launch; add Search Console ownership verification and review the CWV report monthly (see the cadence below).

## 9. Monitoring cadence (from the standard)

| Cadence | Check |
| --- | --- |
| Every deployment | `scripts/seo-smoke-test.sh` (automated in CI) |
| Weekly during launch | Search Console indexing/crawl errors, PageSpeed regression on priority templates, enquiry conversion |
| Monthly | Crawl the site (`Screaming Frog` or equivalent): duplicate metadata, 404s, redirect chains, sitemap hygiene, CWV status |
| Quarterly | Keyword/page mapping, content overlap, outdated claims, internal link structure, access/security review |
| After any URL change | Old→new mapping, redirect verification, sitemap update, canonical validation, Search Console monitoring |

## 10. Acceptance criteria status

| Area | Status |
| --- | --- |
| Indexability | Met — `index,follow` on public routes in production; `noindex` + `Disallow: /` on non-production deployments; `/api/` excluded. |
| Metadata | Met — unique title, description, canonical and Open Graph values on every route. |
| Status and redirects | Met — one-hop 301s for all legacy and variant URLs; real 404 for unknown slugs and paths. |
| Sitemap | Met — generated from published content; no redirect, 404, noindex or duplicate entries; verified by the release gate. |
| Performance | Partially met — image/font/caching/compression work is done; field and lab measurements still need to be recorded on real devices. |
| Core Web Vitals | Not measurable yet — no field data before launch. Lab diagnostics and the fixes above are in place. |
| Content | Met within available facts — no invented clients, certifications, addresses or outcomes; each service and industry page keeps its own scope, context and internal links. |
| Structured data | Met — `Organization`, `WebSite`, `BreadcrumbList`, `Service`, `BlogPosting` only, matching visible content; no ratings, offers, addresses or awards are claimed. |
| Monitoring | Partially met — automated gate and cadence documented; Search Console verification and analytics still need an account-level decision. |
