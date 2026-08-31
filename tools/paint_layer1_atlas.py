"""
Paint apple / iron armor onto the exact diamond layer_1 UV islands, then install for the mod.

Uses the diamond atlas as a mask so pixels only appear where vanilla layer_1 expects them.
"""
from __future__ import annotations

from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parent.parent
ASSETS = Path(r"C:\Users\hp\.cursor\projects\d-xnestorio-mode\assets")
ARMOR_OUT = ROOT / "src/main/resources/assets/customapples/textures/models/armor"

DIAMOND_MASK = ASSETS / (
    "c__Users_hp_AppData_Roaming_Cursor_User_workspaceStorage_1da29c5ffe78f49bf665a4a0e78eabc5"
    "_images_diamond__1_-5e5e59d6-b0da-4097-b172-53327319712a.png"
)
APPLE_PAINT = ASSETS / "apple_layer_1_atlas.png"
IRON_PAINT = ASSETS / "iron_apple_layer_1_atlas.png"

W, H = 64, 32
UPSCALE = 4

# Helmet UV footprint on layer_1 (texOffs 0,0 box 8x8x8)
HELMET_RECT = (0, 0, 32, 16)  # generous top-left mask slice on 64x32


def load_rgba(path: Path) -> Image.Image:
    return Image.open(path).convert("RGBA")


def to_base(img: Image.Image) -> Image.Image:
    if img.size != (W, H):
        return img.resize((W, H), Image.Resampling.NEAREST)
    return img


def upscale(img: Image.Image) -> Image.Image:
    return img.resize((W * UPSCALE, H * UPSCALE), Image.Resampling.NEAREST)


def mask_pixels(mask: Image.Image) -> set[tuple[int, int]]:
    mask = to_base(mask)
    px = mask.load()
    out: set[tuple[int, int]] = set()
    for y in range(H):
        for x in range(W):
            r, g, b, a = px[x, y]
            if a > 20 and (r + g + b) > 30:
                out.add((x, y))
    return out


def helmet_pixels(mask: set[tuple[int, int]]) -> set[tuple[int, int]]:
    x0, y0, x1, y1 = HELMET_RECT
    return {(x, y) for x, y in mask if x0 <= x < x1 and y0 <= y < y1}


def apply_mask(paint: Image.Image, allowed: set[tuple[int, int]]) -> Image.Image:
    paint = to_base(paint)
    out = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    sp, op = paint.load(), out.load()
    for y in range(H):
        for x in range(W):
            if (x, y) in allowed:
                op[x, y] = sp[x, y]
    return out


def save(name: str, img: Image.Image) -> None:
    ARMOR_OUT.mkdir(parents=True, exist_ok=True)
    path = ARMOR_OUT / name
    upscale(img).save(path)
    print(f"OK {path}")


def main() -> None:
    if not DIAMOND_MASK.exists():
        raise SystemExit(f"Missing diamond mask: {DIAMOND_MASK}")

    all_mask = mask_pixels(load_rgba(DIAMOND_MASK))
    helm_mask = helmet_pixels(all_mask)
    body_mask = all_mask - helm_mask

    if APPLE_PAINT.exists():
        save("apple_layer_1.png", apply_mask(load_rgba(APPLE_PAINT), body_mask))
    else:
        print(f"Skip apple — missing {APPLE_PAINT}")

    if IRON_PAINT.exists():
        save("iron_apple_layer_1.png", apply_mask(load_rgba(IRON_PAINT), helm_mask))
    else:
        print(f"Skip iron helmet — missing {IRON_PAINT}")


if __name__ == "__main__":
    main()
