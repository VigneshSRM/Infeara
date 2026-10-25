#!/usr/bin/env bash
# SEO release gate for the Infeara website.
#
# Checks the rules from the website SEO standard against a running instance:
# HTTP status codes, canonical URLs, robots directives, unique titles and descriptions,
# JSON-LD presence, one-hop redirects for URL variants, robots.txt and sitemap hygiene.
#
# Usage:
#   scripts/seo-smoke-test.sh                       # defaults to http://localhost:8080
#   scripts/seo-smoke-test.sh https://www.example.com
#   scripts/seo-smoke-test.sh http://localhost:8080 https://www.infeara.com
#
# The optional second argument is the *expected canonical origin*. Use it to test a production
# configuration on a local port: the pages must advertise the public origin in their canonical
# links even though the test requests arrive on localhost.
#
# Exit code 0 means every check passed; 1 means at least one release-blocking problem was found.

set -uo pipefail

BASE_URL="${1:-http://localhost:8080}"
BASE_URL="${BASE_URL%/}"
CANONICAL_ORIGIN="${2:-$BASE_URL}"
CANONICAL_ORIGIN="${CANONICAL_ORIGIN%/}"
WORK_DIR="$(mktemp -d)"
trap 'rm -rf "$WORK_DIR"' EXIT

failures=0
checks=0

pass() { checks=$((checks + 1)); printf '  ok   %s\n' "$1"; }
fail() { checks=$((checks + 1)); failures=$((failures + 1)); printf '  FAIL %s\n' "$1"; }

http_status() {
  local code
  code="$(curl -sS -o "$WORK_DIR/body" -w '%{http_code}' "$1" 2>/dev/null)" || code="000"
  printf '%s' "$code"
}

echo "SEO smoke test against ${BASE_URL} (expected canonical origin: ${CANONICAL_ORIGIN})"
echo

# 1. robots.txt and sitemap.xml must be reachable and must not block public content.
status="$(http_status "$BASE_URL/robots.txt")"
if [ "$status" = "200" ] && ! grep -qi '^Disallow: /$' "$WORK_DIR/body"; then
  pass "robots.txt returns 200 and does not disallow the whole site"
else
  fail "robots.txt missing, not 200, or disallowing the whole site (status ${status})"
fi
if grep -qi '^Sitemap: ' "$WORK_DIR/body"; then
  pass "robots.txt declares a sitemap"
else
  fail "robots.txt does not declare a sitemap"
fi

status="$(http_status "$BASE_URL/sitemap.xml")"
if [ "$status" = "200" ]; then
  pass "sitemap.xml returns 200"
else
  fail "sitemap.xml did not return 200 (status ${status})"
fi
grep -o '<loc>[^<]*</loc>' "$WORK_DIR/body" 2>/dev/null | sed 's#</\?loc>##g' > "$WORK_DIR/sitemap-urls" || true
sitemap_count="$(wc -l < "$WORK_DIR/sitemap-urls" | tr -d ' ')"
if [ "${sitemap_count:-0}" -gt 0 ]; then
  pass "sitemap lists ${sitemap_count} URLs"
else
  fail "sitemap lists no URLs"
fi

# 2. Every sitemap URL must return 200 (never a redirect, 404 or 5xx).
while read -r url; do
  [ -z "$url" ] && continue
  code="$(curl -sS -o /dev/null -w '%{http_code}' "$url" 2>/dev/null)" || code="000"
  [ "$code" = "200" ] || fail "sitemap URL is not 200 (${code}): ${url}"
done < "$WORK_DIR/sitemap-urls"
pass "sitemap URLs return 200 (any failures listed above)"

# 3. URL variants must permanently redirect in one hop to the preferred URL.
for variant in "/services/" "/robots.txt/" "/industries/healthcare-life-sciences/" "/index.html" "/blogs.html"; do
  response="$(curl -sSi -m 15 -X GET "$BASE_URL$variant" 2>/dev/null || true)"
  code="$(printf '%s' "$response" | head -1 | awk '{print $2}')"
  target="$(printf '%s' "$response" | tr -d '\r' | awk 'tolower($1)=="location:"{print $2}')"
  if { [ "$code" = "301" ] || [ "$code" = "308" ]; } && [ -n "$target" ]; then
    pass "${variant} permanently redirects to ${target}"
  else
    fail "${variant} should permanently redirect but returned ${code:-no response}"
  fi
done

# 4. Unknown URLs must not return a generic 200 page.
code="$(curl -sS -o /dev/null -w '%{http_code}' "$BASE_URL/this-page-does-not-exist" 2>/dev/null)" || code="000"
if [ "$code" = "404" ] || [ "$code" = "410" ]; then
  pass "unknown URL returns ${code}"
else
  fail "unknown URL should return 404/410 but returned ${code}"
fi

# 5. Per-page metadata: canonical, robots directive, unique titles, descriptions, JSON-LD.
if command -v python3 >/dev/null 2>&1; then
  python3 - "$BASE_URL" "$CANONICAL_ORIGIN" <<'PY'
import json, re, sys, urllib.request

base = sys.argv[1].rstrip('/')
canonical_origin = sys.argv[2].rstrip('/')
pages = [
    '/', '/services', '/services/cybersecurity', '/services/cybersecurity/vulnerability-assessment-penetration-testing',
    '/industries', '/industries/healthcare-life-sciences', '/about', '/clients', '/careers',
    '/careers/open-positions', '/blogs', '/insights', '/newsletters', '/contact', '/privacy', '/terms',
]

def fetch(path):
    with urllib.request.urlopen(base + path, timeout=20) as response:
        return response.read().decode('utf-8', 'replace')

def attr(html, pattern):
    match = re.search(pattern, html, re.I | re.S)
    return match.group(1).strip() if match else ''

titles, descriptions, problems = {}, {}, []
for path in pages:
    try:
        html = fetch(path)
    except Exception as error:                                  # noqa: BLE001
        problems.append(f'could not fetch {path}: {error}')
        continue
    title = attr(html, r'<title[^>]*>(.*?)</title>')
    description = attr(html, r'<meta[^>]+name="description"[^>]+content="([^"]*)"')
    canonical = attr(html, r'<link[^>]+rel="canonical"[^>]+href="([^"]*)"')
    robots = attr(html, r'<meta[^>]+name="robots"[^>]+content="([^"]*)"')
    og_title = attr(html, r'<meta[^>]+property="og:title"[^>]+content="([^"]*)"')
    ldjson = html.count('application/ld+json')

    if not title:
        problems.append(f'{path}: missing <title>')
    if not description:
        problems.append(f'{path}: missing meta description')
    if 'noindex' in robots.lower():
        problems.append(f'{path}: public page is marked noindex ({robots})')
    if canonical != canonical_origin + path:
        problems.append(f'{path}: canonical is {canonical!r}, expected {canonical_origin + path!r}')
    if not og_title:
        problems.append(f'{path}: missing og:title')
    # Structured data must exist whenever the page shows a breadcrumb trail, and must be
    # parseable JSON that mirrors that visible trail (see the SEO standard, section 7).
    shows_breadcrumb = 'service-breadcrumb' in html
    if shows_breadcrumb and ldjson == 0:
        problems.append(f'{path}: page shows a breadcrumb but has no JSON-LD')
    for block in re.findall(r'<script[^>]+application/ld\+json[^>]*>(.*?)</script>', html, re.S):
        try:
            parsed = json.loads(block)
        except ValueError as error:
            problems.append(f'{path}: JSON-LD is not valid JSON ({error})')
            continue
        types = {node.get('@type') for node in parsed.get('@graph', [])}
        if shows_breadcrumb and 'BreadcrumbList' not in types:
            problems.append(f'{path}: breadcrumb is visible but JSON-LD has no BreadcrumbList')
    if title.lower() in titles:
        problems.append(f'{path}: duplicate title shared with {titles[title.lower()]}')
    else:
        titles[title.lower()] = path
    if description.lower() in descriptions:
        problems.append(f'{path}: duplicate meta description shared with {descriptions[description.lower()]}')
    else:
        descriptions[description.lower()] = path
    if len(title) > 70:
        problems.append(f'{path}: title is {len(title)} characters, shorten it')

for problem in problems:
    print(f'  FAIL {problem}')
print(f'  checked {len(pages)} pages for metadata')
sys.exit(1 if problems else 0)
PY
  if [ $? -ne 0 ]; then failures=$((failures + 1)); else pass "per-page metadata checks passed"; fi
else
  echo '  skip python3 not available: per-page metadata checks'
fi

echo
if [ "$failures" -gt 0 ]; then
  echo "SEO release gate FAILED (${failures} problem groups found)"
  exit 1
fi
echo "SEO release gate passed (${checks} checks)"
