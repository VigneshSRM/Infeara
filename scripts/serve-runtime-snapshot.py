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


class Handler(BaseHTTPRequestHandler):
    server_version = "InfearaSnapshot/1.0"
    pages = []

    def do_GET(self):  # noqa: N802
        path = self.path.split("?")[0].split("#")[0]

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

        return self.send_body(404, missing_page(path, self.pages), "text/html; charset=utf-8")

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
