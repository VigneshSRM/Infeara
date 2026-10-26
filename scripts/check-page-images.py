import re
import sys
import urllib.request
import xml.etree.ElementTree as ET
from collections import defaultdict

base = (sys.argv[1] if len(sys.argv) > 1 else "http://localhost:8080").rstrip("/")
ns = "{http://www.sitemaps.org/schemas/sitemap/0.9}"
sitemap = urllib.request.urlopen(base + "/sitemap.xml", timeout=30).read().decode()
paths = sorted({re.sub(r"^https?://[^/]+", "", loc.text) or "/"
                for loc in ET.fromstring(sitemap).iter(ns + "loc")})

use = defaultdict(list)
for path in paths:
    html = urllib.request.urlopen(base + path, timeout=30).read().decode("utf-8", "replace")
    body = html.split("</head>")[-1]
    for src in set(re.findall(r'<img[^>]*src="(/assets/[^"]+)"', body)):
        # The logo and the technology marquee are site furniture, not page imagery.
        if "/brand/" in src or "/technologies/" in src:
            continue
        use[src].append(path)

shared = {src: pages for src, pages in use.items() if len(pages) > 1}
print("checked %d pages, %d distinct content images, %d shared"
      % (len(paths), len(use), len(shared)))
for src, pages in sorted(shared.items(), key=lambda item: -len(item[1]))[:10]:
    print("  FAIL shared image: %s on %d pages (%s)"
          % (src, len(pages), ", ".join(pages[:4])))
sys.exit(1 if shared else 0)
