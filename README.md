# Infeara Technologies website

A responsive, accessible, multi-page website for Infeara Technologies, built with Java 27, Spring Boot 4.1.2, and Thymeleaf server-rendered templates. Java supplies page routes and content; published insights and consultation requests use Supabase. The browser presentation uses semantic HTML, CSS, and JavaScript, as browsers do not run Java directly. The Supabase service-role key is used only on the server and is never sent to the browser.

## Requirements

- Java 27 (JDK)
- Maven 3.6.3 or later
- The Supabase project `infeara` (project ref: `lulmtyzgpksqnrqnloeo`)

## Run locally

Confirm `java -version` reports Java 27 and Maven is installed. The project pins Java 27 in both `pom.xml` and `.java-version`.

1. Copy `.env.example` to `.env` and set `SUPABASE_SERVICE_ROLE_KEY` to the server-only `service_role` key from the Supabase API settings. Never use a publishable/anon key for this variable, commit a real key, or expose the service-role key in frontend code.
2. Export the environment values in your shell. For example:

   ```sh
   set -a
   . ./.env
   set +a
   ```

3. Start the application:

   ```sh
   mvn spring-boot:run
   ```

4. Visit <http://localhost:8080>. The API health check is available at `/api/health`.

If the Supabase environment is not configured, the public website still loads, but enquiry submissions are intentionally not accepted; the form displays an error instead of pretending the request was saved. Published insights use a small local fallback until Supabase is configured.

## Website pages

Spring MVC routes render dedicated Thymeleaf pages. The nine `/services/{slug}` parent pages each show a synchronized autoplay/manual carousel for their six “How we can help” capabilities. Every capability has its own nested detail route (`/services/{parent}/{specialist-slug}`), for 54 total pages; the six cybersecurity pages retain their custom content and the other 48 are generated from their parent service definitions. Detail pages use a Z-pattern composition with a square hero photo followed by a portrait photo carousel, plus an absolute canonical link. Set `SITE_BASE_URL` to the public HTTPS origin in production; canonical links omit query parameters. Article detail routes are generated from slugs, and the Insights list/detail pages read published content from Supabase.

- `/` — Home
- `/services` and `/services/{slug}` — Service directory and top-level detail pages
- `/services/{parent}/{specialist-slug}` — 54 capability-level detail pages across all nine service areas; cybersecurity’s six pages retain dedicated, authored scope and imagery
- `/industries` and `/industries/{slug}` — Industry directory and 24 individual profiles with technology considerations and related service links
- `/careers` — Careers overview with brand positioning, team responsibilities, and culture
- `/careers/open-positions` — Separate job listing page with role filters; no vacancies are currently published
- `/clients` — Client collaboration overview with conditional logo/testimonial sections
- `/about` — About Infeara
- `/blogs` and `/blogs/{slug}` — Blog archive and detailed article views, backed by the published `insights` content
- `/insights` and `/insights/{slug}` — Supabase-backed field notes archive and detailed views
- `/newsletters` and `/newsletters/{slug}` — Newsletter archive and detailed edition views, backed by the private `newsletter_editions` table
- `/contact` — Server-validated consultation form
- `/privacy` and `/terms` — Legal information

The Java Developer example supplied for the careers brief is illustrative, not a verified vacancy, so it is not published as a job. No application form, resume upload, or applicant-data processing is enabled. Those features require a verified opening and implemented safeguards for private storage, authorized access, rate limiting, consent, retention/deletion, and malware scanning. Opening filters remain unavailable while there are no active listings.

Edit published blog and field-note articles in the Supabase `insights` table. Only rows with `status = 'published'` are shown. The table is private to the server role; there is no public CMS or unauthenticated edit endpoint. Newsletter issues are served from the separate RLS-protected `newsletter_editions` table; no newsletter editions or mailing-list signup have been added yet.

## Supabase setup

The `consultation_requests` and `insights` tables have been provisioned in the `infeara` project. Matching, reviewable SQL lives in `supabase/migrations/`.

Row-level security is enabled and `anon` / `authenticated` have no table access. Enquiry inserts and published-insight reads are made server-side by the Java application using the service-role key. Keep that key in the backend deployment's secret manager/environment only. Do not add it to `src/main/resources/static`, browser JavaScript, or a public repository configuration.

Required backend settings:

| Variable | Value |
| --- | --- |
| `SUPABASE_URL` | `https://lulmtyzgpksqnrqnloeo.supabase.co` |
| `SUPABASE_SERVICE_ROLE_KEY` | Server-only Supabase `service_role` key |
| `PORT` | Optional; defaults to `8080` |
| `SITE_BASE_URL` | Canonical origin for absolute page URLs; use the public HTTPS origin in production (local default: `http://localhost:8080`) |

## Deploy

Deploy the Maven/Spring Boot application on a Java 27 host, set the environment variables above, configure `SITE_BASE_URL` to the production HTTPS origin, and serve it over HTTPS. The Thymeleaf templates and static assets are packaged inside the app, so the same origin serves all page routes and `/api/consultations`. Keep the service-role key in the host's secret settings and rotate it if it is ever exposed.

## Before public launch

The supplied strategy blueprint calls for verified contact details, approved customer evidence, confirmed service capabilities, and reviewed legal pages. This build intentionally does not invent phone numbers, client logos, testimonials, case-study outcomes, formal vendor partnerships, or certifications. Add only verified business contact information. Review the seeded field notes, privacy and terms pages against the organisation's voice, actual data handling, and legal requirements before publishing. Consider adding a production rate limiter, spam monitoring, backups, and a retention schedule for enquiry records.
