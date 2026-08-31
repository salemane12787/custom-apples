"""Install Blockbench-style 1024x512 armor textures (full resolution, no downscale)."""
from __future__ import annotations

from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parent.parent
ASSETS = Path(r"C:\Users\hp\.cursor\projects\d-xnestorio-mode\assets")
HD_OUT = ROOT / "src/main/resources/assets/customapples/textures/models/armor/hd"

APPLE_SET = ASSETS / "apple_armor_set_1024.png"
IRON_HELM = ASSETS / "iron_apple_helmet_1024.png"
FIRE_MASK = ASSETS / (
    "c__Users_hp_AppData_Roaming_Cursor_User_workspaceStorage_1da29c5ffe78f49bf665a4a0e78eabc5"
    "_images_apple_chestplate-08427d0e-ef21-4e0a-a074-d656a98e5211.png"
)


def load(path: Path) -> Image.Image:
    return Image.open(path).convert("RGBA")


def mask_from(img: Image.Image, min_sum: int = 40) -> set[tuple[int, int]]:
    px = img.load()
    out: set[tuple[int, int]] = set()
    for y in range(img.height):
        for x in range(img.width):
            r, g, b, a = px[x, y]
            if a > 20 and (r + g + b) > min_sum:
                out.add((x, y))
    return out


def apply_mask(paint: Image.Image, allowed: set[tuple[int, int]]) -> Image.Image:
    out = Image.new("RGBA", paint.size, (0, 0, 0, 0))
    sp, op = paint.load(), out.load()
    for y in range(paint.height):
        for x in range(paint.width):
            if (x, y) in allowed:
                op[x, y] = sp[x, y]
    return out


def helmet_region(w: int, h: int) -> set[tuple[int, int]]:
  # top ~35% of atlas where helmet cubes usually sit in user's layout
    return {(x, y) for x in range(w) for y in range(int(h * 0.35))}


def body_region(w: int, h: int) -> set[tuple[int, int]]:
    return {(x, y) for x in range(w) for y in range(int(h * 0.35), h)}


def save(img: Image.Image, name: str) -> None:
    HD_OUT.mkdir(parents=True, exist_ok=True)
    path = HD_OUT / name
    img.save(path)
    print(f"OK {path} ({img.size[0]}x{img.size[1]})")


def main() -> None:
    if not FIRE_MASK.exists():
        raise SystemExit(f"Need fire reference mask: {FIRE_MASK}")

    mask_img = load(FIRE_MASK)
    w, h = mask_img.size
    all_px = mask_from(mask_img)
    helm_px = helmet_region(w, h) & all_px
    body_px = body_region(w, h) & all_px

    if APPLE_SET.exists():
        apple = load(APPLE_SET)
        if apple.size != mask_img.size:
            apple = apple.resize(mask_img.size, Image.Resampling.LANCZOS)
        save(apply_mask(apple, body_px | all_px), "apple_chestplate.png")

    if IRON_HELM.exists():
        iron = load(IRON_HELM)
        if iron.size != mask_img.size:
            iron = iron.resize(mask_img.size, Image.Resampling.LANCZOS)
        save(apply_mask(iron, helm_px), "iron_apple_helmet.png")


if __name__ == "__main__":
    main()
