"""Generate block face textures from item isometric icons (solid 16x16 faces)."""
from __future__ import annotations

from collections import deque
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parent.parent
ITEM_DIR = ROOT / "src/main/resources/assets/customapples/textures/item"
BLOCK_DIR = ROOT / "src/main/resources/assets/customapples/textures/block"

BLOCKS = ("apple_block", "golden_apple_block")

FALLBACK = {
    "apple_block": ((185, 38, 30), (120, 22, 16), (225, 72, 55)),
    "golden_apple_block": ((218, 175, 42), (140, 105, 22), (248, 215, 78)),
}


def remove_edge_background(img: Image.Image, tolerance: int = 28) -> Image.Image:
    img = img.convert("RGBA")
    w, h = img.size
    px = img.load()
    corners = [px[0, 0][:3], px[w - 1, 0][:3], px[0, h - 1][:3], px[w - 1, h - 1][:3]]

    def is_bg(x: int, y: int) -> bool:
        r, g, b, a = px[x, y]
        if a < 8 or r + g + b < 24:
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


def sample(item: Image.Image, x: float, y: float, fallback: tuple[int, int, int]) -> tuple[int, int, int, int]:
    w, h = item.size
    ix = min(w - 1, max(0, int(round(x))))
    iy = min(h - 1, max(0, int(round(y))))
    r, g, b, a = item.getpixel((ix, iy))
    if a < 8 or r + g + b < 20:
        return (*fallback, 255)
  # skip stem/leaf on sides
    if g > 100 and g > r + 15:
        return (*fallback, 255)
    return (r, g, b, 255)


def make_top(item: Image.Image, fallback: tuple[int, int, int]) -> Image.Image:
    """Full top face from isometric item — stem stays centered."""
    size = 16
    out = Image.new("RGBA", (size, size), (*fallback, 255))
    px = out.load()
    for ty in range(size):
        for tx in range(size):
            u = (tx / (size - 1)) * 2.0 - 1.0
            v = (ty / (size - 1)) * 2.0 - 1.0
            sx = 8.0 + u * 5.5
            sy = 4.0 + v * 5.5
            px[tx, ty] = sample(item, sx, sy, fallback)
    return out


def make_side(item: Image.Image, fallback: tuple[int, int, int]) -> Image.Image:
    """Full side face from left+right isometric panels."""
    size = 16
    out = Image.new("RGBA", (size, size), (*fallback, 255))
    px = out.load()
    for sy in range(size):
        for sx in range(size):
            u = sx / (size - 1)
            v = sy / (size - 1)
            lx = u * 7.5
            ly = 4.0 + v * 11.0 + u * 4.0
            rx = 8.0 + u * 7.5
            ry = 4.0 + v * 11.0 + (1.0 - u) * 4.0
            lr = sample(item, lx, ly, fallback)
            rr = sample(item, rx, ry, fallback)
            px[sx, sy] = (
                (lr[0] + rr[0]) // 2,
                (lr[1] + rr[1]) // 2,
                (lr[2] + rr[2]) // 2,
                255,
            )
    return out


def make_bottom(side: Image.Image, dark: tuple[int, int, int]) -> Image.Image:
    out = Image.new("RGBA", (16, 16), (*dark, 255))
    spx = side.load()
    opx = out.load()
    for y in range(16):
        for x in range(16):
            r, g, b, _ = spx[x, y]
            opx[x, y] = (int(r * 0.72), int(g * 0.72), int(b * 0.72), 255)
    return out


def generate(name: str) -> None:
    base, dark, light = FALLBACK[name]
    item = remove_edge_background(Image.open(ITEM_DIR / f"{name}.png")).resize(
        (16, 16), Image.Resampling.NEAREST
    )
    top = make_top(item, light)
    side = make_side(item, base)
    bottom = make_bottom(side, dark)
    BLOCK_DIR.mkdir(parents=True, exist_ok=True)
    top.save(BLOCK_DIR / f"{name}_top.png")
    side.save(BLOCK_DIR / f"{name}_side.png")
    bottom.save(BLOCK_DIR / f"{name}_bottom.png")
    side.save(BLOCK_DIR / f"{name}.png")
    print(f"OK {name} faces")


def main() -> None:
    for name in BLOCKS:
        generate(name)


if __name__ == "__main__":
    main()
