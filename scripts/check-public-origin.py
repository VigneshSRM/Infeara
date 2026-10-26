#!/usr/bin/env python3
"""Checks that a running instance publishes the expected canonical origin everywhere.

Run this against an instance started with the production configuration:

    SITE_BASE_URL=https://www.infeara.com SITE_INDEXABLE=true java -jar target/*.jar
    python3 scripts/check-public-origin.py http://localhost:8080 https://www.infeara.com

Every check prints what the server actually returned when it fails, so a mismatch is diagnosable
without access to the job log.
"""
import re
import sys
import urllib.error
import urllib.request

base = (sys.argv[1] if len(sys.argv) > 1 else "http://localhost:8080").rstrip("/")
origin = (sys.argv[2] if len(sys.argv) > 2 else "https://www.infeara.com").rstrip("/")

failures = 0


def fetch(path, host=None, follow=True):
    request = urllib.request.Request(base + path)
    if host:
        request.add_header("Host", host)
    opener = urllib.request.build_opener() if follow else urllib.request.build_opener(NoRedirect)
    try:
        with opener.open(request, timeout=20) as response:
            return response.status, response.read().decode("utf-8", "replace"), dict(response.headers)
    except urllib.error.HTTPError as error:
        return error.code, error.read().decode("utf-8", "replace"), dict(error.headers)


class NoRedirect(urllib.request.HTTPRedirectHandler):
    def redirect_request(self, *_args, **_kwargs):
        return None


def check(name, condition, detail):
    global failures
    if condition:
        print("  ok   %s" % name)
    else:
        failures += 1
        print("  FAIL %s" % name)
        print("       got: %s" % detail)


print("Checking the public origin %s on %s" % (origin, base))
print()

status, home, _ = fetch("/")
check("home page returns 200", status == 200, status)

canonical = re.search(r'<link[^>]+rel="canonical"[^>]+href="([^"]*)"', home)
check("canonical on / is %s/" % origin,
      bool(canonical) and canonical.group(1) == origin + "/",
      canonical.group(1) if canonical else "no canonical link")

robots = re.search(r'<meta[^>]+name="robots"[^>]+content="([^"]*)"', home)
check("robots meta is index,follow on /",
      bool(robots) and robots.group(1) == "index,follow",
      robots.group(1) if robots else "no robots meta")

og_url = re.search(r'<meta[^>]+property="og:url"[^>]+content="([^"]*)"', home)
check("og:url on / is the public origin",
      bool(og_url) and og_url.group(1) == origin + "/",
      og_url.group(1) if og_url else "no og:url")

status, sitemap, _ = fetch("/sitemap.xml")
locs = re.findall(r"<loc>([^<]+)</loc>", sitemap)
off_origin = [url for url in locs if not url.startswith(origin + "/")]
check("sitemap lists only %s URLs" % origin, status == 200 and not off_origin,
      "%d URLs, first off-origin: %s" % (len(locs), off_origin[0] if off_origin else "none"))
check("sitemap lists 103 URLs", len(locs) == 103, len(locs))

status, robots_txt, _ = fetch("/robots.txt")
check("robots.txt points at the public sitemap",
      ("Sitemap: %s/sitemap.xml" % origin) in robots_txt,
      [line for line in robots_txt.splitlines() if line.lower().startswith("sitemap")])

canonical_host = origin.split("//", 1)[-1]
if canonical_host.startswith("www."):
    bare_host = canonical_host[4:]
    status, _, headers = fetch("/services", host=bare_host, follow=False)
    location = headers.get("Location", "")
    check("%s redirects in one hop to %s/services" % (bare_host, origin),
          status == 301 and location == origin + "/services",
          "%s %s" % (status, location))
else:
    print("  ..   no www prefix on %s, so there is no bare domain to redirect" % canonical_host)

status, _, _ = fetch("/services", host=origin.split("//", 1)[-1], follow=False)
check("%s is served directly, not redirected" % origin.split("//", 1)[-1],
      status == 200, status)

print()
if failures:
    print("%d check(s) failed" % failures)
    sys.exit(1)
print("All public origin checks passed")
