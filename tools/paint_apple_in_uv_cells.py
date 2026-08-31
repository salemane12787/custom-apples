"""
Paint apple armor into the exact UV wireframe cells (helmet, chest, boots)
from the user's Blockbench UV screenshot.
"""
from __future__ import annotations

from pathlib import Path

from PIL import Image, ImageDraw, ImageFilter

ROOT = Path(__file__).resolve().parent.parent
ASSETS = Path(r"C:\Users\hp\.cursor\projects\d-xnestorio-mode\assets")
HD_OUT = ROOT / "src/main/resources/assets/customapples/textures/models/armor/hd"
BLOCKBENCH_OUT = ROOT / "blockbench"

UV_WIREFRAME = ASSETS / (
    "c__Users_hp_AppData_Roaming_Cursor_User_workspaceStorage_1da29c5ffe78f49bf665a4a0e78eabc5"
    "_images_image-7a46caa1-1f46-4824-aad8-80bc1320b64a.png"
)
FIRE_PATTERN = ASSETS / (
    "c__Users_hp_AppData_Roaming_Cursor_User_workspaceStorage_1da29c5ffe78f49bf665a4a0e78eabc5"
    "_images_apple_chestplate-8fdd6dbe-8e4d-495b-8128-8f19ed5b3f6c.png"
)

OUT_W, OUT_H = 1024, 512


def line_pixels(gray: Image.Image) -> set[tuple[int, int]]:
    px = gray.load()
    w, h = gray.size
    lines: set[tuple[int, int]] = set()
    for y in range(h):
        for x in range(w):
            if px[x, y] < 128:
                lines.add((x, y))
    return lines


def cell_mask_from_wireframe(gray: Image.Image) -> Image.Image:
    """White pixels inside UV cells (touching a wire line)."""
    lines = line_pixels(gray)
    if not lines:
        raise SystemExit("No wireframe lines detected")

    xs = [p[0] for p in lines]
    ys = [p[1] for p in lines]
    xmin, xmax = min(xs), max(xs)
    ymin, ymax = min(ys), max(ys)

    mask = Image.new("L", gray.size, 0)
    mp = gray.load()
    op = mask.load()

    for y in range(ymin, ymax + 1):
        for x in range(xmin, xmax + 1):
            if mp[x, y] <= 200:
                continue
            for dx in range(-1, 2):
                for dy in range(-1, 2):
                    if (x + dx, y + dy) in lines:
                        op[x, y] = 255
                        break

    return mask


def apple_color_from_fire(r: int, g: int, b: int, a: int) -> tuple[int, int, int, int]:
    if a < 12:
        return (0, 0, 0, 0)
    lum = (r + g + b) / 3
    is_gold = r > 130 and g > 85 and b < 130 and r >= g
    is_bright = lum > 175 and r > 140
    is_dark = lum < 72

    if is_gold:
        return (215, 170, 60, a)
    if is_bright:
        return (255, 215, 100, a)
    if is_dark:
        return (48, 10, 16, a)
    if g > r and g > 70:
        return (38, 115, 42, a)
    return (min(255, int(r * 1.05)), max(0, int(g * 0.42)), max(0, int(b * 0.42)), a)


def build_apple_pattern(size: tuple[int, int]) -> Image.Image:
    if not FIRE_PATTERN.exists():
        raise SystemExit(f"Missing pattern source: {FIRE_PATTERN}")
    fire = Image.open(FIRE_PATTERN).convert("RGBA").resize(size, Image.Resampling.LANCZOS)
    out = Image.new("RGBA", size, (0, 0, 0, 0))
    sp, op = fire.load(), out.load()
    w, h = size
    for y in range(h):
        for x in range(w):
            op[x, y] = apple_color_from_fire(*sp[x, y])
    return out.filter(ImageFilter.SHARPEN)


def region_tint(img: Image.Image, mask: Image.Image, mult: tuple[float, float, float]) -> None:
    mr, mg, mb = mult
    ip, mp = img.load(), mask.load()
    w, h = img.size
    for y in range(h):
        for x in range(w):
            if mp[x, y] < 128:
                continue
            r, g, b, a = ip[x, y]
            ip[x, y] = (min(255, int(r * mr)), min(255, int(g * mg)), min(255, int(b * mb)), a)


def label_regions(mask: Image.Image) -> dict[str, Image.Image]:
    """Split UV mask into helmet / chest / boots by vertical bands on wireframe."""
    w, h = mask.size
    mp = mask.load()
    helm = Image.new("L", (w, h), 0)
    chest = Image.new("L", (w, h), 0)
    boots = Image.new("L", (w, h), 0)
    hp, cp, bp = helm.load(), chest.load(), boots.load()

    # wireframe layout: helmet top ~38%, chest middle ~32%, boots bottom + right cluster
    helm_y = int(h * 0.38)
    chest_y = int(h * 0.72)
    boot_x = int(w * 0.55)

    for y in range(h):
        for x in range(w):
            if mp[x, y] < 128:
                continue
            if y < helm_y:
                hp[x, y] = 255
            elif y < chest_y:
                cp[x, y] = 255
            elif x >= boot_x or y >= chest_y:
                bp[x, y] = 255
            else:
                cp[x, y] = 255

    return {"helmet": helm, "chest": chest, "boots": boots}


def compose(mask: Image.Image, pattern: Image.Image) -> Image.Image:
    out = Image.new("RGBA", mask.size, (0, 0, 0, 0))
    regions = label_regions(mask)
    layer = pattern.copy()
    # subtle per-piece tint so helmet/boots read differently on same atlas
    region_tint(layer, regions["helmet"], (0.92, 0.92, 0.98))
    region_tint(layer, regions["boots"], (1.05, 0.88, 0.88))

    mp, lp, op = mask.load(), layer.load(), out.load()
    w, h = mask.size
    for y in range(h):
        for x in range(w):
            if mp[x, y] > 128:
                op[x, y] = lp[x, y]
    return out


def save(img: Image.Image) -> None:
    for base in (HD_OUT, BLOCKBENCH_OUT):
        base.mkdir(parents=True, exist_ok=True)
        for name in ("apple_chestplate.png", "apple_armor_texture.png"):
            p = base / name
            img.save(p)
            print(f"OK {p} ({img.size[0]}x{img.size[1]})")


def main() -> None:
    if not UV_WIREFRAME.exists():
        raise SystemExit(f"Missing UV wireframe: {UV_WIREFRAME}")

    gray = Image.open(UV_WIREFRAME).convert("L")
    cell_mask = cell_mask_from_wireframe(gray)
    cell_mask = cell_mask.resize((OUT_W, OUT_H), Image.Resampling.NEAREST)

    pattern = build_apple_pattern((OUT_W, OUT_H))
    result = compose(cell_mask, pattern)

    save(result)

    # Iron apple: helmet UV cells only
    iron = Image.new("RGBA", (OUT_W, OUT_H), (0, 0, 0, 0))
    regions = label_regions(cell_mask)
    helm_mask = regions["helmet"]
    ip, lp, hp = iron.load(), pattern.load(), helm_mask.load()
    for y in range(OUT_H):
        for x in range(OUT_W):
            if hp[x, y] > 128:
                r, g, b, a = lp[x, y]
                avg = (r + g + b) / 3
                ip[x, y] = (int(min(255, avg * 1.1)), int(min(255, avg * 1.08)), int(min(255, avg * 1.15)), a)

    for base in (HD_OUT, BLOCKBENCH_OUT):
        base.mkdir(parents=True, exist_ok=True)
        p = base / "iron_apple_helmet.png"
        iron.save(p)
        print(f"OK {p}")

    preview = Image.new("RGBA", (OUT_W, OUT_H), (255, 255, 255, 255))
    preview.paste(result, (0, 0), result)
    preview_path = ASSETS / "apple_uv_painted_preview.png"
    preview.save(preview_path)
    print(f"OK preview {preview_path}")


if __name__ == "__main__":
    main()
