"""
Install a flat Minecraft humanoid UV atlas (layer_1) for the apple chestplate.

Use a reference image already laid out like vanilla armor — NOT separate 3D face renders.
Reference should be 64x32 logical UV, any integer upscale (e.g. 1024x512 = 16x).
"""
from __future__ import annotations

from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parent.parent
ASSETS = Path(r"C:\Users\hp\.cursor\projects\d-xnestorio-mode\assets")
ARMOR_OUT = ROOT / "src/main/resources/assets/customapples/textures/models/armor"

DEFAULT_REF = ASSETS / (
    "c__Users_hp_AppData_Roaming_Cursor_User_workspaceStorage_1da29c5ffe78f49bf665a4a0e78eabc5"
    "_images_5e0da985-1e1b-48dd-b623-0fea2649ed85-cbec6bfd-b733-4679-bfa1-55f4f08dfc5d.jpg"
)

W, H = 64, 32
UPSCALE = 4


def white_to_alpha(img: Image.Image, min_rgb: int = 248) -> Image.Image:
    img = img.convert("RGBA")
    px = img.load()
    for y in range(img.height):
        for x in range(img.width):
            r, g, b, a = px[x, y]
            if r >= min_rgb and g >= min_rgb and b >= min_rgb:
                px[x, y] = (0, 0, 0, 0)
    return img


def logical_size(img: Image.Image) -> tuple[int, int]:
    w, h = img.size
    if w % W != 0 or h % H != 0:
        raise SystemExit(f"Image {w}x{h} is not a whole multiple of {W}x{H}")
    return w // W, h // H


def to_base_atlas(img: Image.Image) -> Image.Image:
    fx, fy = logical_size(img)
    if fx != fy:
        # Non-uniform scale still OK — resize to exact 64x32
        return img.resize((W, H), Image.Resampling.NEAREST)
    if fx == 1:
        return img.copy()
    return img.resize((W, H), Image.Resampling.NEAREST)


def upscale_nearest(layer: Image.Image, factor: int = UPSCALE) -> Image.Image:
    return layer.resize((W * factor, H * factor), Image.Resampling.NEAREST)


def install(reference: Path, out_name: str = "apple_chestplate_layer_1.png") -> None:
    if not reference.exists():
        raise SystemExit(f"Missing reference: {reference}")

    src = Image.open(reference)
    src = white_to_alpha(src)
    base = to_base_atlas(src)
    out = upscale_nearest(base)

    ARMOR_OUT.mkdir(parents=True, exist_ok=True)
    out_path = ARMOR_OUT / out_name
    out.save(out_path)
    print(f"OK {out_path} ({out.size[0]}x{out.size[1]}, from {src.size[0]}x{src.size[1]} ref)")


def main() -> None:
    install(DEFAULT_REF)


if __name__ == "__main__":
    main()
