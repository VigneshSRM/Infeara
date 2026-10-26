#!/usr/bin/env python3
"""Serves the pages that CI recorded from the running Spring Boot application.

This environment has no JDK and cannot reach Maven Central, so the application is built and
run by the CI workflow instead (see .github/workflows/seo.yml). The workflow commits the exact
response bodies it served to docs/runtime-snapshot/, and this script puts those responses on a
local port with CSS, JS and images mapped to the repository's static resources.

Usage:
    python3 scripts/serve-runtime-snapshot.py [port]      # default 8080
"""
import os
import sys
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer

REPO = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
SNAPSHOT = os.path.join(REPO, "docs", "runtime-snapshot")
STATIC = os.path.join(REPO, "src", "main", "resources", "static")

CONTENT_TYPES = {
    ".html": "text/html; charset=utf-8",
    ".css": "text/css; charset=utf-8",
    ".js": "text/javascript; charset=utf-8",
    ".svg": "image/svg+xml",
    ".png": "image/png",
    ".jpg": "image/jpeg",
    ".jpeg": "image/jpeg",
    ".webp": "image/webp",
    ".ico": "image/x-icon",
    ".xml": "application/xml; charset=utf-8",
    ".txt": "text/plain; charset=utf-8",
    ".woff2": "font/woff2",
}


def recorded_pages():
    pages = []
    for root, _dirs, files in os.walk(SNAPSHOT):
        if "index.html" in files:
            relative = os.path.relpath(root, SNAPSHOT)
            pages.append("/" if relative == "." else "/" + relative.replace(os.sep, "/"))
    return sorted(pages)


def missing_page(path, pages):
    links = "\n".join('<li><a href="%s">%s</a></li>' % (page, page) for page in pages)
    body = (
        "<!doctype html><html lang=\"en\"><head><meta charset=\"utf-8\">"
        "<title>%s was not recorded</title><style>body{font:14px/1.6 system-ui;margin:60px auto;"
        "max-width:760px;padding:0 24px;color:#24313d}code{background:#f2efe8;padding:2px 6px}"
        "h1{font-size:21px}ul{columns:2;padding-left:18px;font-size:13px}a{color:#226192}</style>"
        "</head><body><h1><code>%s</code> is not in this snapshot</h1>"
        "<p>CI recorded %d routes from the running application. This path was not one of them "
        "(a typo, or a route that only exists as a redirect).</p><ul>%s</ul>"
        "<p><a href=\"/\">Back to the home page</a></p></body></html>"
        % (path, path, len(pages), links))
    return body.encode("utf-8")


def canonical_path(path):
    """The path the application would redirect to, mirroring SeoRequestFilter.

    The site serves every page without a trailing slash and never from an index.html file, and
    legacy .html addresses move to the extension-less path.
    """
    candidate = path
    if candidate.endswith("/index.html"):
        candidate = candidate[: -len("index.html")]
    elif candidate.endswith(".html") and candidate != "/404.html":
        candidate = candidate[: -len(".html")]
        if candidate == "/index":
            candidate = "/"
    while len(candidate) > 1 and candidate.endswith("/"):
        candidate = candidate[:-1]
    if not candidate:
        candidate = "/"
    return None if candidate == path else candidate


class Handler(BaseHTTPRequestHandler):
    server_version = "InfearaSnapshot/1.0"
    pages = []

    def do_GET(self):  # noqa: N802
        path = self.path.split("?")[0].split("#")[0]

        preferred = canonical_path(path)
        if preferred is not None:
            return self.redirect(preferred)

        # The workspace needs a signed-in session, so it is not part of a recorded snapshot. The
        # application redirects an anonymous caller to the sign-in page, and so does the preview.
        if path == "/cms":
            return self.redirect("/cms/login")

        if path.startswith(("/css/", "/js/", "/assets/")):
            target = os.path.join(STATIC, path.lstrip("/"))
            if os.path.isfile(target):
                return self.send_file(target)
            return self.send_body(404, b"Not found", "text/plain; charset=utf-8")

        if path in ("/robots.txt", "/sitemap.xml"):
            target = os.path.join(SNAPSHOT, path.lstrip("/"))
            if os.path.isfile(target):
                return self.send_file(target)

        candidate = os.path.join(SNAPSHOT, path.strip("/"), "index.html")
        if os.path.isfile(candidate):
            return self.send_file(candidate)

        # The recorded page for an unknown URL is served with the status the application sent, so
        # the preview behaves like the real site for a typo as well.
        if path == "/_recorded-routes":
            return self.send_body(200, missing_page(path, self.pages), "text/html; charset=utf-8")

        not_found = os.path.join(SNAPSHOT, "404.html")
        if os.path.isfile(not_found):
            with open(not_found, "rb") as handle:
                return self.send_body(404, handle.read(), "text/html; charset=utf-8")

        return self.send_body(404, missing_page(path, self.pages), "text/html; charset=utf-8")

    def do_POST(self):  # noqa: N802
        """Explains itself instead of failing: the preview serves recorded bodies, not an app."""
        path = self.path.split("?")[0].split("#")[0]
        if path in ("/cms/login", "/cms/logout"):
            body = (
                "<!doctype html><html lang=\"en\"><head><meta charset=\"utf-8\">"
                "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">"
                "<title>Sign-in is verified in CI, not in this preview</title>"
                "<style>body{font:15px/1.7 system-ui;margin:70px auto;max-width:620px;"
                "padding:0 24px;color:#24313d}code{background:#f2efe8;padding:2px 6px}"
                "h1{font-size:22px;letter-spacing:-.02em}a{color:#226192}</style></head><body>"
                "<h1>This preview serves recorded pages</h1>"
                "<p>The sign-in page you just submitted is the exact HTML the running application "
                "returned, served as a static snapshot, so there is no server here to authenticate "
                "you.</p>"
                "<p>The real sign-in is exercised on every push by <code>scripts/check-cms-login.sh</code> "
                "against the application running on Java 27: CSRF rejection, generic errors, lockout, "
                "the hardened session cookie, the workspace and sign-out.</p>"
                "<p><a href=\"/cms/login\">Back to the sign-in page</a></p></body></html>"
            ).encode("utf-8")
            return self.send_body(200, body, "text/html; charset=utf-8")
        return self.send_body(405, b"Method not allowed", "text/plain; charset=utf-8")

    def redirect(self, location):
        self.send_response(301)
        self.send_header("Location", location)
        self.send_header("Content-Length", "0")
        self.send_header("X-Source", "CI runtime snapshot")
        self.end_headers()

    def send_file(self, target):
        extension = os.path.splitext(target)[1].lower()
        with open(target, "rb") as handle:
            self.send_body(200, handle.read(),
                           CONTENT_TYPES.get(extension, "application/octet-stream"))

    def send_body(self, status, body, content_type):
        self.send_response(status)
        self.send_header("Content-Type", content_type)
        self.send_header("Content-Length", str(len(body)))
        self.send_header("X-Source", "CI runtime snapshot")
        self.end_headers()
        self.wfile.write(body)

    def log_message(self, fmt, *args):
        print("%s - %s" % (self.address_string(), fmt % args), flush=True)


if __name__ == "__main__":
    Handler.pages = recorded_pages()
    port = int(sys.argv[1]) if len(sys.argv) > 1 else int(os.environ.get("PORT", "8080"))
    print("Serving %d recorded pages from docs/runtime-snapshot on http://0.0.0.0:%d"
          % (len(Handler.pages), port), flush=True)
    ThreadingHTTPServer(("0.0.0.0", port), Handler).serve_forever()
