#!/usr/bin/env python3
"""Generates one distinct image per page from the studio photography in static/assets.

The site has 24 photographs and 87 content pages, so the same file used to appear on up to 35
pages. This script derives a separate file for every page instead: each image is a different crop
(zoom and framing), sometimes mirrored, with its own colour grade, chosen deterministically from
the page's slug so the result is stable across runs and machines.

Output:
    static/assets/services/pages/<parent>-<slug>-<n>.webp    capability pages
    static/assets/industries/pages/<slug>.webp               industry pages
    static/assets/editorial/pages/<slug>.webp                field notes on the home page

Run after changing the page inventory:

    pip install Pillow
    python3 scripts/generate-page-images.py
"""
import hashlib
import os
import re
import sys

from PIL import Image, ImageEnhance

REPO = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
ASSETS = os.path.join(REPO, "src", "main", "resources", "static", "assets")
CONTENT = os.path.join(REPO, "src", "main", "resources", "content")

CAPABILITY_SIZE = (960, 520)
INDUSTRY_SIZE = (1264, 848)
EDITORIAL_SIZE = (640, 360)
QUALITY = 74


def seed_for(text):
    """Stable seed: Python's hash() is salted per process, so use a digest instead."""
    return int.from_bytes(hashlib.sha256(text.encode("utf-8")).digest()[:8], "big")


def sources():
    """Every studio photograph that may be used as a base, grouped by folder."""
    groups = {}
    for folder in ("photos", "services", "industries", "editorial", "hero", "careers"):
        directory = os.path.join(ASSETS, folder)
        files = sorted(name for name in os.listdir(directory)
                       if name.endswith((".jpg", ".png")) and os.path.isfile(os.path.join(directory, name)))
        groups[folder] = [os.path.join(directory, name) for name in files]
    return groups


def derive(source, target_size, seed, grade_shift):
    """Crops, optionally mirrors and grades one source photograph into a distinct image."""
    image = Image.open(source).convert("RGB")
    width, height = image.size
    target_w, target_h = target_size
    target_ratio = target_w / target_h
    source_ratio = width / height

    # Zoom in by a page-specific factor, then frame the crop off-centre but inside the image.
    zoom = 1.0 + ((seed >> 8) % 55) / 100.0                     # 1.00 - 1.54
    if source_ratio > target_ratio:
        crop_h = height / zoom
        crop_w = crop_h * target_ratio
    else:
        crop_w = width / zoom
        crop_h = crop_w / target_ratio
    slack_x = max(0.0, width - crop_w)
    slack_y = max(0.0, height - crop_h)
    offset_x = slack_x * (((seed >> 16) % 100) / 100.0)
    offset_y = slack_y * (((seed >> 24) % 100) / 100.0)
    box = (int(offset_x), int(offset_y), int(offset_x + crop_w), int(offset_y + crop_h))
    image = image.crop(box)

    if seed & 1:
        image = image.transpose(Image.FLIP_LEFT_RIGHT)

    image = image.resize(target_size, Image.LANCZOS)

    # A slightly different grade per page, so two crops of one photograph do not read as one image.
    colour, contrast, brightness = grade_shift
    image = ImageEnhance.Color(image).enhance(colour)
    image = ImageEnhance.Contrast(image).enhance(contrast)
    image = ImageEnhance.Brightness(image).enhance(brightness)
    return image


def grade_for(seed):
    colours = (0.92, 0.98, 1.04, 1.1)
    contrasts = (0.95, 1.0, 1.05, 1.1)
    brightnesses = (0.96, 0.99, 1.02, 1.05)
    return (colours[(seed >> 32) % 4], contrasts[(seed >> 40) % 4], brightnesses[(seed >> 48) % 4])


def save(image, path):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    image.save(path, "WEBP", quality=QUALITY, method=6)
    return os.path.getsize(path)


def capability_pages():
    """(parent, slug) pairs, read from the copy library so the list cannot drift."""
    text = open(os.path.join(CONTENT, "page-copy-capabilities.txt"), encoding="utf-8").read()
    return re.findall(r"@capability (\S+)/(\S+)", text)


def industry_pages():
    text = open(os.path.join(CONTENT, "page-copy-industries.txt"), encoding="utf-8").read()
    return re.findall(r"@industry (\S+)", text)


def service_overviews():
    """The nine service area pages, each with three of its own images."""
    path = os.path.join(REPO, "src", "main", "java", "com", "infeara", "site", "content", "WebsiteContent.java")
    text = open(path, encoding="utf-8").read()
    return re.findall(r'new ServicePage\("([a-z-]+)"', text)


def cybersecurity_pages():
    path = os.path.join(REPO, "src", "main", "java", "com", "infeara", "site", "content", "WebsiteContent.java")
    text = open(path, encoding="utf-8").read()
    return re.findall(r'new SpecialistServicePage\("cybersecurity", "([a-z0-9-]+)"', text)


def field_notes():
    path = os.path.join(REPO, "src", "main", "java", "com", "infeara", "site", "content", "Insight.java")
    text = open(path, encoding="utf-8").read()
    return re.findall(r'"([a-z0-9-]+)", "services/[a-z-]+\.jpg"', text)


def main():
    pool = sources()
    # Capability pages draw on the whole studio set; industries keep their sector photograph as the
    # base (recognition matters there) but get their own framing of it.
    # Every operational photograph takes part, so no source is asked to cover more than a handful of
    # pages. Sector, editorial and careers imagery is included because each page is still a distinct
    # crop, mirror and grade of its source rather than a copy of it.
    all_photos = (pool["photos"] + pool["services"] + pool["industries"]
                  + pool["editorial"] + pool["hero"] + pool["careers"])
    industry_photo = {}
    for name in os.listdir(os.path.join(ASSETS, "industries")):
        if name.endswith(".jpg"):
            industry_photo[re.sub(r"\.jpg$", "", name)] = os.path.join(ASSETS, "industries", name)

    written, total = [], 0

    for parent, slug in capability_pages():
        seed = seed_for(parent + "/" + slug)
        for index in range(2):
            s = seed + index * 7919
            source = all_photos[(s >> 3) % len(all_photos)]
            image = derive(source, CAPABILITY_SIZE, s, grade_for(s))
            name = "pages/%s-%s-%d.webp" % (parent, slug, index + 1)
            total += save(image, os.path.join(ASSETS, "services", name))
            written.append(("services", name))

    for slug in service_overviews():
        seed = seed_for("service/" + slug)
        for index in range(3):
            s = seed + index * 6151
            source = all_photos[(s >> 5) % len(all_photos)]
            image = derive(source, CAPABILITY_SIZE, s, grade_for(s))
            name = "pages/service-%s-%d.webp" % (slug, index + 1)
            total += save(image, os.path.join(ASSETS, "services", name))
            written.append(("services", name))

    for slug in cybersecurity_pages():
        seed = seed_for("cyber/" + slug)
        for index in range(2):
            s = seed + index * 4289
            source = all_photos[(s >> 7) % len(all_photos)]
            image = derive(source, CAPABILITY_SIZE, s, grade_for(s))
            name = "pages/cybersecurity-%s-%d.webp" % (slug, index + 1)
            total += save(image, os.path.join(ASSETS, "services", name))
            written.append(("services", name))

    # Thumbnails for the capability carousel on each service area page: the carousel is a preview of
    # the linked capability page, so it gets its own small image rather than republishing that page's.
    for parent, slug in capability_pages():
        seed = seed_for("thumb/" + parent + "/" + slug)
        source = all_photos[(seed >> 9) % len(all_photos)]
        image = derive(source, (480, 260), seed, grade_for(seed))
        name = "pages/%s-%s-thumb.webp" % (parent, slug)
        total += save(image, os.path.join(ASSETS, "services", name))
        written.append(("services", name))

    for slug in industry_pages():
        seed = seed_for("industry/" + slug)
        # The sector photograph is chosen by the same mapping the application uses; if a sector has
        # no dedicated file, fall back to the wider set.
        base = industry_photo.get(slug)
        source = base if base else all_photos[seed % len(all_photos)]
        image = derive(source, INDUSTRY_SIZE, seed, grade_for(seed))
        name = "pages/%s.webp" % slug
        total += save(image, os.path.join(ASSETS, "industries", name))
        written.append(("industries", name))

    for slug in field_notes():
        seed = seed_for("note/" + slug)
        source = pool["editorial"][seed % len(pool["editorial"])]
        image = derive(source, EDITORIAL_SIZE, seed, grade_for(seed))
        name = "pages/%s.webp" % slug
        total += save(image, os.path.join(ASSETS, "editorial", name))
        written.append(("editorial", name))

    print("wrote %d images, %.1f MB total" % (len(written), total / (1024 * 1024)))
    print("  capability: %d" % sum(1 for group, _ in written if group == "services"))
    print("  industry:   %d" % sum(1 for group, _ in written if group == "industries"))
    print("  field note: %d" % sum(1 for group, _ in written if group == "editorial"))


if __name__ == "__main__":
    sys.exit(main())
