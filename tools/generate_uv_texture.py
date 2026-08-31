"""
Schema-driven UV texture generator.

Reads a JSON schema + UV reference image, detects islands, paints each island
with a registered generator, outputs an exact UV-aligned PNG.

Usage:
  python tools/generate_uv_texture.py iron_apple_helmet
  python tools/generate_uv_texture.py apple_chestplate
  python tools/generate_uv_texture.py --schema path/to/schema.json
"""
from __future__ import annotations

import argparse
import json
import math
import random
from pathlib import Path
from typing import Callable

from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parent.parent
SCHEMA_DIR = ROOT / "tools" / "textures" / "schema"
ASSETS = Path(r"C:\Users\hp\.cursor\projects\d-xnestorio-mode\assets")
OUT_DIR = ASSETS / "generated"


# ---------------------------------------------------------------------------
# UV island detection
# ---------------------------------------------------------------------------

def load_uv_mask(ref_path: Path, threshold: int = 28) -> tuple[Image.Image, int, int]:
    img = Image.open(ref_path).convert("RGBA")
    w, h = img.size
    mask = Image.new("L", (w, h), 0)
    src, dst = img.load(), mask.load()
    for y in range(h):
        for x in range(w):
            r, g, b, a = src[x, y]
            if a > 20 and r + g + b > threshold:
                dst[x, y] = 255
    return mask, w, h


def split_mask_columns(mask: Image.Image, min_gap: int = 2) -> list[Image.Image]:
    """Split a mask into sub-masks where fully-empty vertical column gaps exist."""
    w, h = mask.size
    px = mask.load()
    gaps: list[int] = []
    for x in range(w):
        if all(px[x, y] < 128 for y in range(h)):
            gaps.append(x)
    if not gaps:
        return [mask]
    runs: list[tuple[int, int]] = []
    start = 0
    for g in gaps:
        if g - start >= min_gap:
            runs.append((start, g))
        start = g + 1
    if w - start >= min_gap:
        runs.append((start, w))
    parts: list[Image.Image] = []
    for x0, x1 in runs:
        part = Image.new("L", (w, h), 0)
        sp, dp = mask.load(), part.load()
        for y in range(h):
            for x in range(x0, x1):
                if sp[x, y] > 128:
                    dp[x, y] = 255
        if any(dp[x, y] > 128 for y in range(h) for x in range(w)):
            parts.append(part)
    return parts if parts else [mask]


def find_islands(mask: Image.Image) -> list[dict]:
    w, h = mask.size
    px = mask.load()
    visited = [[False] * w for _ in range(h)]
    islands: list[dict] = []

    for sy in range(h):
        for sx in range(w):
            if px[sx, sy] < 128 or visited[sy][sx]:
                continue
            stack = [(sx, sy)]
            cells: list[tuple[int, int]] = []
            visited[sy][sx] = True
            while stack:
                x, y = stack.pop()
                cells.append((x, y))
                for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                    nx, ny = x + dx, y + dy
                    if 0 <= nx < w and 0 <= ny < h and not visited[ny][nx] and px[nx, ny] > 128:
                        visited[ny][nx] = True
                        stack.append((nx, ny))
            xs = [c[0] for c in cells]
            ys = [c[1] for c in cells]
            cx = sum(xs) / len(cells)
            cy = sum(ys) / len(cells)
            islands.append({
                "cells": cells,
                "bbox": (min(xs), min(ys), max(xs), max(ys)),
                "cx": cx,
                "cy": cy,
                "w": max(xs) - min(xs) + 1,
                "h": max(ys) - min(ys) + 1,
            })
    return islands


def islands_from_mask(mask: Image.Image) -> list[dict]:
    islands = find_islands(mask)
    if len(islands) <= 1:
        expanded: list[dict] = []
        for part in split_mask_columns(mask):
            expanded.extend(find_islands(part))
        if expanded:
            return expanded
    return islands


def label_head_cross(islands: list[dict]) -> dict[str, dict]:
    """Minecraft head/helmet cross: top, left, front, right, back, bottom."""
    if not islands:
        return {}
    by_cy = sorted(islands, key=lambda i: i["cy"])
    top = by_cy[0]
    bottom = by_cy[-1]
    middle = [i for i in islands if i not in (top, bottom)]
    middle.sort(key=lambda i: i["cx"])
    labels: dict[str, dict] = {"top": top, "bottom": bottom}
    if len(middle) >= 4:
        labels["left"] = middle[0]
        labels["front"] = middle[1]
        labels["right"] = middle[2]
        labels["back"] = middle[3]
    elif len(middle) == 3:
        labels["left"] = middle[0]
        labels["front"] = middle[1]
        labels["right"] = middle[2]
    elif len(middle) == 1:
        labels["front"] = middle[0]
    return labels


def label_chestplate(islands: list[dict]) -> dict[str, dict]:
    """Torso panels left, arm pieces right."""
    if not islands:
        return {}
    islands = sorted(islands, key=lambda i: (-i["w"] * i["h"], -i["cx"]))
    labels: dict[str, dict] = {}
    large = sorted(islands, key=lambda i: i["w"] * i["h"], reverse=True)
    if len(large) >= 2:
        torso = sorted(large[:2], key=lambda i: i["cx"])
        labels["torso_front"] = torso[0]
        labels["torso_back"] = torso[1]
    elif len(large) == 1:
        labels["torso_front"] = large[0]
    arms = [i for i in islands if i not in labels.values()]
    arms.sort(key=lambda i: i["cy"])
    if len(arms) >= 2:
        labels["arm_left"] = arms[0]
        labels["arm_right"] = arms[1]
    elif len(arms) == 1:
        labels["arm_left"] = arms[0]
    return labels


def island_from_bbox_norm(bbox_norm: list[float], bw: int, bh: int) -> dict:
    x0 = max(0, int(bbox_norm[0] * bw))
    y0 = max(0, int(bbox_norm[1] * bh))
    x1 = min(bw - 1, int(bbox_norm[2] * bw) - 1)
    y1 = min(bh - 1, int(bbox_norm[3] * bh) - 1)
    if x1 < x0 or y1 < y0:
        x1, y1 = x0, y0
    cells = [(x, y) for y in range(y0, y1 + 1) for x in range(x0, x1 + 1)]
    cx = (x0 + x1) / 2
    cy = (y0 + y1) / 2
    return {
        "cells": cells,
        "bbox": (x0, y0, x1, y1),
        "cx": cx,
        "cy": cy,
        "w": x1 - x0 + 1,
        "h": y1 - y0 + 1,
    }


LABELERS = {
    "head_cross": label_head_cross,
    "chestplate": label_chestplate,
    "manual": lambda islands: {},
}


# ---------------------------------------------------------------------------
# Painting helpers
# ---------------------------------------------------------------------------

def island_mask(island: dict, scale: int) -> Image.Image:
    cells = island["cells"]
    x0, y0, x1, y1 = island["bbox"]
    w, h = (x1 - x0 + 1) * scale, (y1 - y0 + 1) * scale
    m = Image.new("L", (w, h), 0)
    mp = m.load()
    for x, y in cells:
        for sy in range(scale):
            for sx in range(scale):
                mp[(x - x0) * scale + sx, (y - y0) * scale + sy] = 255
    return m


def paste_island(canvas: Image.Image, island: dict, piece: Image.Image, scale: int) -> None:
    x0, y0, _, _ = island["bbox"]
    canvas.paste(piece, (x0 * scale, y0 * scale), piece)


def metal_color(t: float, rust: float = 0.0) -> tuple[int, int, int]:
    base = 95 + t * 90
    r = int(base + rust * 40)
    g = int(base * 0.95 + rust * 20)
    b = int(base * 0.92 + rust * 10)
    return (min(255, r), min(255, g), min(255, b))


def draw_iron_apple(view: str, w: int, h: int, rng: random.Random) -> Image.Image:
    img = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    cx, cy = w / 2, h / 2

    # Brushed iron base
    for y in range(h):
        for x in range(w):
            noise = rng.randint(-8, 8)
            c = metal_color(0.35 + 0.15 * math.sin(x * 0.3), 0.05)
            img.putpixel((x, y), (max(0, c[0] + noise), max(0, c[1] + noise), max(0, c[2] + noise), 255))

    def apple_alpha(x: float, y: float) -> float:
        if view == "top":
            dx, dy = (x - cx) / (w * 0.38), (y - cy) / (h * 0.38)
            return max(0.0, 1.0 - dx * dx - dy * dy)
        if view == "side":
            dx = (x - cx) / (w * 0.32)
            dy = (y - cy) / (h * 0.42)
            return max(0.0, 1.0 - dx * dx - dy * dy * 0.85)
        if view == "back":
            dx = (x - cx) / (w * 0.35)
            dy = (y - cy) / (h * 0.4)
            return max(0.0, 1.0 - dx * dx - dy * dy)
        # front
        dx = (x - cx) / (w * 0.36)
        dy = (y - (cy - h * 0.05)) / (h * 0.42)
        return max(0.0, 1.0 - dx * dx - dy * dy * 0.9)

    px = img.load()
    for y in range(h):
        for x in range(w):
            a = apple_alpha(x, y)
            if a <= 0:
                continue
            hl = max(0, 1 - ((x - cx + w * 0.12) ** 2 + (y - cy + h * 0.1) ** 2) / (w * w * 0.15))
            rust = rng.random() * 0.15 if rng.random() < 0.08 else 0
            t = 0.45 + hl * 0.45 + a * 0.1
            c = metal_color(t, rust)
            px[x, y] = (c[0], c[1], c[2], 255)

    # Stem + leaf (top and front)
    if view in ("top", "front", "side"):
        stem_w = max(2, w // 16)
        stem_h = max(3, h // 8)
        sx = int(cx - stem_w // 2)
        sy = int(cy - h * 0.38)
        draw.rectangle([sx, sy, sx + stem_w, sy + stem_h], fill=(55, 40, 30, 255))
        lx = int(cx + stem_w)
        ly = int(sy + stem_h // 3)
        draw.polygon([
            (lx, ly),
            (lx + w // 6, ly - h // 10),
            (lx + w // 5, ly + h // 12),
        ], fill=(45, 120, 55, 255))

    return img


def draw_plain_iron(w: int, h: int, rng: random.Random) -> Image.Image:
    img = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    px = img.load()
    for y in range(h):
        for x in range(w):
            noise = rng.randint(-12, 12)
            rust = 0.12 if rng.random() < 0.06 else 0.03
            c = metal_color(0.3 + 0.1 * math.sin(x * 0.25), rust)
            px[x, y] = (max(0, c[0] + noise), max(0, c[1] + noise), max(0, c[2] + noise), 255)
    return img


def draw_ornate_chest(part: str, w: int, h: int, rng: random.Random) -> Image.Image:
    img = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    px = img.load()

    # Red velvet base with vertical folds
    for y in range(h):
        for x in range(w):
            fold = 0.85 + 0.15 * math.sin(x * math.pi / max(4, w // 8))
            edge = min(x, w - 1 - x, y, h - 1 - y) / max(1, min(w, h) * 0.15)
            edge = min(1.0, edge)
            r = int((120 + 40 * fold) * edge)
            g = int((15 + 8 * fold) * edge)
            b = int((25 + 10 * fold) * edge)
            px[x, y] = (r, g, b, 255)

    # Gold border frame
    border = max(2, min(w, h) // 12)
    gold = (200, 165, 60, 255)
    gold_hi = (255, 220, 100, 255)
    draw.rectangle([0, 0, w - 1, h - 1], outline=gold_hi, width=border)
    draw.rectangle([border, border, w - 1 - border, h - 1 - border], outline=gold, width=max(1, border // 2))

    # Corner filigree curls
    curl_r = min(w, h) // 5
    for ox, oy in ((border, border), (w - border, border), (border, h - border), (w - border, h - border)):
        draw.arc([ox - curl_r, oy - curl_r, ox + curl_r, oy + curl_r], 0, 90, fill=gold_hi, width=max(2, border // 2))

    if part == "front":
        # Golden apple crest center
        acx, acy = w // 2, int(h * 0.52)
        ar = min(w, h) // 4
        for y in range(h):
            for x in range(w):
                d = math.hypot(x - acx, y - acy)
                if d < ar:
                    t = 1 - d / ar
                    px[x, y] = (int(180 + 60 * t), int(130 + 70 * t), int(30 + 20 * t), 255)
        draw.rectangle([acx - 2, acy - ar - 4, acx + 2, acy - ar], fill=(60, 45, 30, 255))
        draw.polygon([
            (acx + 3, acy - ar - 2),
            (acx + ar // 3, acy - ar - ar // 4),
            (acx + ar // 4, acy - ar),
        ], fill=(50, 130, 60, 255))
        # Ruby gem bottom
        gx, gy = w // 2, h - border - min(w, h) // 8
        gr = min(w, h) // 10
        draw.ellipse([gx - gr, gy - gr, gx + gr, gy + gr], fill=(200, 30, 50, 255))
        draw.ellipse([gx - gr // 2, gy - gr // 2, gx + gr // 2, gy + gr // 2], fill=(255, 80, 90, 255))

    elif part == "back":
        # Apple tree silhouette
        tcx, tcy = w // 2, int(h * 0.35)
        draw.rectangle([tcx - 3, tcy, tcx + 3, int(h * 0.75)], fill=(80, 60, 25, 255))
        for angle in range(0, 360, 45):
            rad = math.radians(angle)
            bx = tcx + int(math.cos(rad) * w * 0.28)
            by = tcy + int(math.sin(rad) * h * 0.2)
            draw.line([(tcx, tcy), (bx, by)], fill=gold, width=max(2, border // 2))
            br = min(w, h) // 12
            draw.ellipse([bx - br, by - br, bx + br, by + br], fill=(200, 150, 40, 255))

    elif part == "arm":
        # Scale mail + ruby gem
        step = max(4, min(w, h) // 6)
        for yy in range(0, h, step):
            for xx in range(0, w, step):
                if (xx + yy) % (step * 2) == 0:
                    draw.arc([xx, yy, xx + step, yy + step], 0, 180, fill=gold_hi, width=1)
        gx, gy = w // 2, h // 2
        gr = min(w, h) // 5
        draw.ellipse([gx - gr, gy - gr, gx + gr, gy + gr], fill=(180, 25, 45, 255))
        draw.ellipse([gx - gr // 2, gy - gr // 2, gx + gr // 2, gy + gr // 2], fill=(255, 70, 85, 255))

    # Vine accents on borders
    for i in range(3):
        vy = border + i * (h - 2 * border) // 3
        draw.arc([border, vy - h // 8, w - border, vy + h // 8], 0, 180, fill=(50, 130, 55, 200), width=2)

    return img


def draw_ornate_leggings(part: str, w: int, h: int, rng: random.Random) -> Image.Image:
    img = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    px = img.load()
    border = max(2, min(w, h) // 10)
    gold = (200, 165, 60, 255)
    gold_hi = (255, 220, 100, 255)

    for y in range(h):
        for x in range(w):
            fold = 0.85 + 0.15 * math.sin(x * math.pi / max(4, w // 6))
            r = int(110 + 45 * fold)
            g = int(12 + 10 * fold)
            b = int(22 + 12 * fold)
            px[x, y] = (r, g, b, 255)

    draw.rectangle([0, 0, w - 1, h - 1], outline=gold_hi, width=border)
    draw.rectangle([border, border, w - 1 - border, h - 1 - border], outline=gold, width=max(1, border // 2))

    if part == "waist":
        for i in range(4):
            yy = border + i * (h - 2 * border) // 4
            draw.line([(border, yy), (w - border, yy)], fill=gold, width=max(1, border // 2))
        acx, acy = w // 2, h // 2
        ar = min(w, h) // 5
        draw.ellipse([acx - ar, acy - ar, acx + ar, acy + ar], fill=(200, 150, 40, 255))
    else:
        # Leg panel — vertical gold vines + small apple emblem
        step = max(3, h // 8)
        for yy in range(border, h - border, step):
            draw.arc([border, yy, w - border, yy + step * 2], 0, 180, fill=gold_hi, width=2)
        acx, acy = w // 2, int(h * 0.45)
        ar = min(w, h) // 5
        for y in range(h):
            for x in range(w):
                d = math.hypot(x - acx, y - acy)
                if d < ar:
                    t = 1 - d / ar
                    px[x, y] = (int(170 + 55 * t), int(120 + 60 * t), int(25 + 20 * t), 255)
        draw.rectangle([acx - 1, acy - ar - 3, acx + 1, acy - ar], fill=(55, 40, 28, 255))
        draw.polygon([
            (acx + 2, acy - ar - 1),
            (acx + ar // 3, acy - ar - ar // 5),
            (acx + ar // 4, acy - ar),
        ], fill=(48, 125, 58, 255))

    return img


def draw_ornate_boots(part: str, w: int, h: int, rng: random.Random) -> Image.Image:
    img = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    px = img.load()
    border = max(2, min(w, h) // 8)
    gold = (200, 165, 60, 255)
    gold_hi = (255, 220, 100, 255)

    for y in range(h):
        for x in range(w):
            fold = 0.8 + 0.2 * math.sin(x * 0.4)
            r = int(105 + 50 * fold)
            g = int(10 + 12 * fold)
            b = int(20 + 15 * fold)
            px[x, y] = (r, g, b, 255)

    draw.rectangle([0, 0, w - 1, h - 1], outline=gold_hi, width=border)

    if part == "top":
        for x in range(border, w - border, max(2, w // 6)):
            draw.line([(x, border), (x, h - border)], fill=gold, width=1)
    else:
        # Boot body — gold toe cap + apple stud
        toe_h = h // 3
        draw.rectangle([border, h - toe_h, w - border, h - border], fill=(180, 145, 50, 255))
        acx, acy = w // 2, h // 2
        ar = min(w, h) // 4
        for y in range(h):
            for x in range(w):
                d = math.hypot(x - acx, y - acy)
                if d < ar:
                    t = 1 - d / ar
                    px[x, y] = (int(175 + 50 * t), int(125 + 65 * t), int(28 + 18 * t), 255)
        gr = max(2, min(w, h) // 8)
        draw.ellipse([w // 2 - gr, h - toe_h - gr, w // 2 + gr, h - toe_h + gr], fill=(220, 40, 55, 255))

    return img


GENERATORS: dict[str, Callable[..., Image.Image]] = {
    "iron_apple": lambda w, h, opts, rng: draw_iron_apple(opts.get("view", "front"), w, h, rng),
    "plain_iron": lambda w, h, opts, rng: draw_plain_iron(w, h, rng),
    "ornate_chest": lambda w, h, opts, rng: draw_ornate_chest(opts.get("part", "front"), w, h, rng),
    "ornate_leggings": lambda w, h, opts, rng: draw_ornate_leggings(opts.get("part", "leg"), w, h, rng),
    "ornate_boots": lambda w, h, opts, rng: draw_ornate_boots(opts.get("part", "body"), w, h, rng),
}


# ---------------------------------------------------------------------------
# Main pipeline
# ---------------------------------------------------------------------------

def resolve_ref(path_str: str) -> Path:
    p = Path(path_str)
    if p.is_file():
        return p
    return ASSETS / path_str


def generate(schema_path: Path, out_path: Path | None = None) -> Path:
    schema = json.loads(schema_path.read_text(encoding="utf-8"))
    ref = resolve_ref(schema["uv_reference"])
    scale = int(schema.get("scale", 8))
    layout = schema.get("layout", "head_cross")
    island_cfg = schema.get("islands", {})

    mask, bw, bh = load_uv_mask(ref)
    out_w, out_h = bw * scale, bh * scale
    canvas = Image.new("RGBA", (out_w, out_h), (0, 0, 0, 0))
    rng = random.Random(schema.get("seed", 42))

    if layout == "manual":
        labeled = {}
        for label, cfg in island_cfg.items():
            if "bbox_norm" in cfg:
                labeled[label] = island_from_bbox_norm(cfg["bbox_norm"], bw, bh)
            elif "bbox" in cfg:
                x0, y0, x1, y1 = cfg["bbox"]
                labeled[label] = island_from_bbox_norm(
                    [x0 / bw, y0 / bh, x1 / bw, y1 / bh], bw, bh
                )
    else:
        islands = islands_from_mask(mask)
        labeled = LABELERS.get(layout, label_head_cross)(islands)
        if not labeled:
            raise SystemExit(f"No UV islands detected in {ref}")

    for label, cfg in island_cfg.items():
        island = labeled.get(label)
        if island is None:
            print(f"WARN: no island for label '{label}'")
            continue
        gen_name = cfg.get("generator", "plain_iron")
        gen_fn = GENERATORS.get(gen_name)
        if gen_fn is None:
            raise SystemExit(f"Unknown generator: {gen_name}")

        x0, y0, x1, y1 = island["bbox"]
        iw, ih = (x1 - x0 + 1) * scale, (y1 - y0 + 1) * scale
        piece = gen_fn(iw, ih, cfg, rng)
        imask = island_mask(island, scale)
        clipped = Image.new("RGBA", (iw, ih), (0, 0, 0, 0))
        clipped.paste(piece, (0, 0), imask)
        paste_island(canvas, island, clipped, scale)
        print(f"  painted {label} ({iw}x{ih}) at ({x0 * scale},{y0 * scale}) -> {gen_name}")

    OUT_DIR.mkdir(parents=True, exist_ok=True)
    name = schema.get("name", schema_path.stem)
    if out_path is None:
        out_path = OUT_DIR / f"{name}.png"
    canvas.save(out_path)
    print(f"OK {out_w}x{out_h} -> {out_path}")
    return out_path


def main() -> None:
    parser = argparse.ArgumentParser(description="Generate UV texture from schema")
    parser.add_argument("schema", nargs="?", help="Schema name (without .json) or path")
    parser.add_argument("--schema", dest="schema_path", help="Full path to schema JSON")
    parser.add_argument("-o", "--output", help="Output PNG path")
    parser.add_argument("--all", action="store_true", help="Generate all schemas in schema dir")
    args = parser.parse_args()

    if args.all:
        for p in sorted(SCHEMA_DIR.glob("*.json")):
            print(f"\n=== {p.name} ===")
            generate(p)
        return

    if args.schema_path:
        path = Path(args.schema_path)
    elif args.schema:
        path = SCHEMA_DIR / args.schema
        if not path.suffix:
            path = path.with_suffix(".json")
        if not path.is_file():
            path = Path(args.schema)
    else:
        parser.print_help()
        return

    out = Path(args.output) if args.output else None
    generate(path, out)


if __name__ == "__main__":
    main()
