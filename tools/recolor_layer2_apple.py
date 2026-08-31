"""
Color-key the layer_2 UV mask into Apple Armor palette.

Preserves exact occupancy: only existing mask pixels are written.
Empty atlas space stays fully transparent. No fill, no AA.
"""
from __future__ import annotations

from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parent.parent
ASSETS = Path(r"C:\Users\hp\.cursor\projects\d-xnestorio-mode\assets")
VANILLA = ROOT / "src/main/resources/assets/customapples/textures/models/armor"
HD = VANILLA / "hd"
BB = ROOT / "blockbench"
GEN = ASSETS / "generated"

MASK = ASSETS / (
    "c__Users_hp_AppData_Roaming_Cursor_User_workspaceStorage_1da29c5ffe78f49bf665a4a0e78eabc5"
    "_images_layer_2-5bd2ef95-2358-4dc2-8769-35482b12eeed.png"
)

LEAF_GREEN = (0x56, 0xCB, 0x19, 255)
APPLE_RED = (0xCE, 0x12, 0x12, 255)
STEM_BROWN = (0x5C, 0x3D, 0x2E, 255)
EMPTY = (0, 0, 0, 0)

# Darkest mask tones = internal seams (luminance < 42).
SEAM_LUMA_MAX = 42

# Topmost 4x4 protrusion (waist top / leaf).
LEAF_BOX = (4, 16, 8, 20)  # x0, y0, x1, y1 exclusive


def luma(r: int, g: int, b: int) -> float:
    return (r + g + b) / 3.0


def is_filled(r: int, g: int, b: int, a: int) -> bool:
    return a >= 12 and (r + g + b) >= 12


def in_leaf(x: int, y: int) -> bool:
    x0, y0, x1, y1 = LEAF_BOX
    return x0 <= x < x1 and y0 <= y < y1


def recolor(mask: Image.Image) -> Image.Image:
    src = mask.convert("RGBA")
    out = Image.new("RGBA", src.size, EMPTY)
    sp, op = src.load(), out.load()
    for y in range(src.height):
        for x in range(src.width):
            r, g, b, a = sp[x, y]
            if not is_filled(r, g, b, a):
                continue
            if in_leaf(x, y):
                op[x, y] = LEAF_GREEN
            elif luma(r, g, b) <= SEAM_LUMA_MAX:
                op[x, y] = STEM_BROWN
            else:
                op[x, y] = APPLE_RED
    return out


def occupancy(img: Image.Image) -> set[tuple[int, int]]:
    px = img.convert("RGBA").load()
    w, h = img.size
    cells: set[tuple[int, int]] = set()
    for y in range(h):
        for x in range(w):
            r, g, b, a = px[x, y]
            if is_filled(r, g, b, a):
                cells.add((x, y))
    return cells


def save(img: Image.Image, path: Path) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    img.save(path)
    print(f"OK {path} ({img.size[0]}x{img.size[1]})")


def preview(img: Image.Image, scale: int = 16) -> Image.Image:
    """Checkerboard-backed nearest upscale so 64x32 pixels are inspectable."""
    big = img.resize((img.width * scale, img.height * scale), Image.Resampling.NEAREST)
    board = Image.new("RGBA", big.size, (0, 0, 0, 255))
    bp = board.load()
    cell = scale
    for y in range(big.height):
        for x in range(big.width):
            if ((x // cell) + (y // cell)) % 2:
                bp[x, y] = (24, 24, 24, 255)
            else:
                bp[x, y] = (8, 8, 8, 255)
    board.paste(big, (0, 0), big)
    return board


def main() -> None:
    if not MASK.exists():
        raise SystemExit(f"Missing mask: {MASK}")

    mask = Image.open(MASK)
    result = recolor(mask)

    src_occ = occupancy(mask)
    dst_occ = occupancy(result)
    if src_occ != dst_occ:
        extra = dst_occ - src_occ
        missing = src_occ - dst_occ
        raise SystemExit(f"Occupancy mismatch extra={len(extra)} missing={len(missing)}")

    px = result.load()
    counts = {LEAF_GREEN: 0, APPLE_RED: 0, STEM_BROWN: 0}
    for x, y in dst_occ:
        counts[px[x, y]] += 1

    print(f"pixels={len(dst_occ)} green={counts[LEAF_GREEN]} red={counts[APPLE_RED]} brown={counts[STEM_BROWN]}")

    for dest in (VANILLA, HD, BB, GEN):
        save(result, dest / "apple_layer_2.png")

    save(preview(result), GEN / "apple_layer_2_preview.png")
    print("Done — exact mask occupancy, Apple palette only.")


if __name__ == "__main__":
    main()
