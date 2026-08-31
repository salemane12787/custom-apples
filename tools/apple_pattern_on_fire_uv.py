"""Keep fire armor UV islands; paint apple pattern only inside those pixels."""
from __future__ import annotations

from pathlib import Path

from PIL import Image, ImageDraw, ImageFilter

ROOT = Path(__file__).resolve().parent.parent
ASSETS = Path(r"C:\Users\hp\.cursor\projects\d-xnestorio-mode\assets")
HD_OUT = ROOT / "src/main/resources/assets/customapples/textures/models/armor/hd"
BLOCKBENCH_OUT = ROOT / "blockbench"

FIRE_UV = ASSETS / (
    "c__Users_hp_AppData_Roaming_Cursor_User_workspaceStorage_1da29c5ffe78f49bf665a4a0e78eabc5"
    "_images_apple_chestplate-8fdd6dbe-8e4d-495b-8128-8f19ed5b3f6c.png"
)
APPLE_PAINT = ASSETS / "apple_armor_from_fire_uv.png"


def mask_pixels(img: Image.Image) -> set[tuple[int, int]]:
    px = img.load()
    out: set[tuple[int, int]] = set()
    for y in range(img.height):
        for x in range(img.width):
            r, g, b, a = px[x, y]
            if a > 20 and r + g + b > 35:
                out.add((x, y))
    return out


def apple_pattern_from_fire(fire: Image.Image) -> Image.Image:
    """Rebuild apple theme using fire UV as structure guide (pattern swap, not 1:1 recolor)."""
    fire = fire.convert("RGBA")
    w, h = fire.size
    out = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    sp, op = fire.load(), out.load()
    mask = mask_pixels(fire)

    # Soft apple paint for blending detail
    paint = Image.open(APPLE_PAINT).convert("RGBA") if APPLE_PAINT.exists() else None
    if paint and paint.size != fire.size:
        paint = paint.resize(fire.size, Image.Resampling.LANCZOS)
    pp = paint.load() if paint else None

    for y in range(h):
        for x in range(w):
            if (x, y) not in mask:
                continue
            r, g, b, a = sp[x, y]
            lum = (r + g + b) / 3
            is_gold = r > 130 and g > 85 and b < 130 and r >= g
            is_bright = lum > 175 and r > 150
            is_dark = lum < 70

            if pp is not None:
                pr, pg, pb, pa = pp[x, y]
                if pa > 40:
                    # 70% apple paint detail, 30% structure from fire luminance
                    br = int(pr * 0.7 + r * 0.3)
                    bg = int(pg * 0.7 + g * 0.3)
                    bb = int(pb * 0.7 + b * 0.3)
                    op[x, y] = (br, bg, bb, a)
                    continue

            if is_gold:
                op[x, y] = (210, 165, 55, a)  # gold trim
            elif is_bright:
                op[x, y] = (255, 220, 120, a)  # apple highlight
            elif is_dark:
                op[x, y] = (55, 12, 18, a)  # burgundy shadow
            elif g > r and g > 80:
                op[x, y] = (40, 110, 45, a)  # leaf green accent on greenish fire pixels
            else:
                op[x, y] = (min(255, int(r * 1.05)), max(0, int(g * 0.4)), max(0, int(b * 0.4)), a)

    # Tiny leaf accents on some gold trim pixels
    draw = ImageDraw.Draw(out)
    for y in range(h):
        for x in range(w):
            r, g, b, a = op[x, y]
            if a < 20 or r < 180 or g < 120 or b > 90:
                continue
            if (x * 7 + y * 13) % 97 == 0:
                draw.point((x, y), fill=(35, 120, 40, a))

    return out.filter(ImageFilter.SHARPEN)


def save(img: Image.Image, name: str) -> None:
    for base in (HD_OUT, BLOCKBENCH_OUT):
        base.mkdir(parents=True, exist_ok=True)
        p = base / name
        img.save(p)
        print(f"OK {p}")


def main() -> None:
    if not FIRE_UV.exists():
        raise SystemExit(f"Missing fire UV reference: {FIRE_UV}")
    fire = Image.open(FIRE_UV).convert("RGBA")
    apple = apple_pattern_from_fire(fire)
    save(apple, "apple_chestplate.png")
    save(apple, "apple_armor_texture.png")
    print("Done — apple pattern on YOUR fire UV islands.")


if __name__ == "__main__":
    main()
