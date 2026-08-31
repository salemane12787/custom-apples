"""Install AI-generated block face textures (top/side) and derive bottoms."""
from __future__ import annotations

from pathlib import Path

from PIL import Image

ASSETS = Path(r"C:\Users\hp\.cursor\projects\d-xnestorio-mode\assets")
BLOCK_DIR = Path(__file__).resolve().parent.parent / "src/main/resources/assets/customapples/textures/block"
ENTITY_BELL = Path(__file__).resolve().parent.parent / "src/main/resources/assets/customapples/textures/entity/bell"

FACES = {
    "apple_block_top_gen": "apple_block_top.png",
    "apple_block_side_gen": "apple_block_side.png",
    "golden_apple_block_top_gen": "golden_apple_block_top.png",
    "golden_apple_block_side_gen": "golden_apple_block_side.png",
}

BLOCK_KEYS = ("apple_block", "golden_apple_block")


def remove_edge_background(img: Image.Image, tolerance: int = 28) -> Image.Image:
    from collections import deque

    img = img.convert("RGBA")
    w, h = img.size
    px = img.load()
    corners = [px[0, 0][:3], px[w - 1, 0][:3], px[0, h - 1][:3], px[w - 1, h - 1][:3]]

    def is_bg(x: int, y: int) -> bool:
        r, g, b, a = px[x, y]
        if a < 8:
            return True
        return any(abs(r - cr) <= tolerance and abs(g - cg) <= tolerance and abs(b - cb) <= tolerance
                   for cr, cg, cb in corners)

    seen: set[tuple[int, int]] = set()
    q: deque[tuple[int, int]] = deque()
    for x in range(w):
        q.append((x, 0))
        q.append((x, h - 1))
    for y in range(h):
        q.append((0, y))
        q.append((w - 1, y))
    while q:
        x, y = q.popleft()
        if (x, y) in seen or x < 0 or y < 0 or x >= w or y >= h:
            continue
        if not is_bg(x, y):
            continue
        seen.add((x, y))
        q.extend([(x + 1, y), (x - 1, y), (x, y + 1), (x, y - 1)])
    for x, y in seen:
        r, g, b, _ = px[x, y]
        px[x, y] = (r, g, b, 0)
    return img


def solidify_face(img: Image.Image, size: int = 16) -> Image.Image:
    """Resize to a full solid block face — no flat color fill."""
    img = img.convert("RGBA")
    if img.size != (size, size):
        img = img.resize((size, size), Image.Resampling.NEAREST)
    px = img.load()
    opaque = [px[x, y][:3] for y in range(size) for x in range(size) if px[x, y][3] >= 8]
    if not opaque:
        return img
    br = sum(c[0] for c in opaque) // len(opaque)
    bg = sum(c[1] for c in opaque) // len(opaque)
    bb = sum(c[2] for c in opaque) // len(opaque)
    out = Image.new("RGBA", (size, size), (br, bg, bb, 255))
    out.paste(img, (0, 0), img)
    return out


def to_face(src_name: str, out_name: str, size: int = 16) -> bool:
    src = ASSETS / f"{src_name}.png"
    if not src.exists():
        print(f"MISSING {src_name}")
        return False
    img = Image.open(src).convert("RGBA")
    if max(img.size) > 512:
        img.thumbnail((512, 512), Image.Resampling.NEAREST)
    img = remove_edge_background(img)
    face = solidify_face(img, size)
    BLOCK_DIR.mkdir(parents=True, exist_ok=True)
    face.save(BLOCK_DIR / out_name)
    print(f"OK {out_name}")
    return True


def darken_side_to_bottom(side_name: str, bottom_name: str, factor: float = 0.7) -> None:
    side = Image.open(BLOCK_DIR / side_name).convert("RGBA")
    out = side.copy()
    px = out.load()
    for y in range(side.height):
        for x in range(side.width):
            r, g, b, a = px[x, y]
            if a:
                px[x, y] = (int(r * factor), int(g * factor), int(b * factor), a)
    out.save(BLOCK_DIR / bottom_name)
    print(f"OK {bottom_name}")


def copy_bell_body_to_block() -> None:
    src = ENTITY_BELL / "bell_body.png"
    if not src.exists():
        print("MISSING entity bell_body — run install_ai_texture.py --placed-blocks first")
        return
    BLOCK_DIR.mkdir(parents=True, exist_ok=True)
    Image.open(src).save(BLOCK_DIR / "apple_bell_body.png")
    print("OK apple_bell_body.png (for block atlas)")


def main() -> None:
    for src, out in FACES.items():
        to_face(src, out)
    darken_side_to_bottom("apple_block_side.png", "apple_block_bottom.png")
    darken_side_to_bottom("golden_apple_block_side.png", "golden_apple_block_bottom.png")
    copy_bell_body_to_block()


if __name__ == "__main__":
    main()
