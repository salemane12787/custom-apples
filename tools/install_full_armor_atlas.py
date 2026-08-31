"""
Install a full Minecraft armor atlas PNG at native resolution (no downscale / no pixelization).

Vanilla humanoid armor uses TWO files per material:
  layer_1.png — helmet (if any), chestplate, boots
  layer_2.png — leggings

Place your painted atlases in assets/ as:
  apple_layer_1_source.png
  apple_layer_2_source.png

Or pass paths as arguments. UV layout must match the standard 64x32 humanoid map
(can be any integer upscale, e.g. 1024x512 = 16x).

Iron apple helmet (separate item) uses iron_apple_layer_1.png (helmet UV only).
"""
from __future__ import annotations

import sys
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parent.parent
ASSETS = Path(r"C:\Users\hp\.cursor\projects\d-xnestorio-mode\assets")
ARMOR_OUT = ROOT / "src/main/resources/assets/customapples/textures/models/armor"


def clear_background(img: Image.Image) -> Image.Image:
    img = img.convert("RGBA")
    px = img.load()
    for y in range(img.height):
        for x in range(img.width):
            r, g, b, a = px[x, y]
            if a < 8:
                continue
            if r >= 248 and g >= 248 and b >= 248:
                px[x, y] = (0, 0, 0, 0)
            elif r <= 24 and g <= 24 and b <= 24:
                px[x, y] = (0, 0, 0, 0)
    return img


def install(src: Path, dest_name: str) -> None:
    if not src.exists():
        raise SystemExit(f"Missing: {src}")
    img = clear_background(Image.open(src))
    ARMOR_OUT.mkdir(parents=True, exist_ok=True)
    out = ARMOR_OUT / dest_name
    img.save(out)
    print(f"OK {out} ({img.size[0]}x{img.size[1]}, full resolution)")


def main() -> None:
    layer1 = ASSETS / "apple_layer_1_source.png"
    layer2 = ASSETS / "apple_layer_2_source.png"
    if len(sys.argv) >= 2:
        layer1 = Path(sys.argv[1])
    if len(sys.argv) >= 3:
        layer2 = Path(sys.argv[2])

    if layer1.exists():
        install(layer1, "apple_layer_1.png")
    else:
        print(f"Skip layer_1 — add {layer1}")

    if layer2.exists():
        install(layer2, "apple_layer_2.png")
    else:
        print(f"Skip layer_2 — add {layer2}")


if __name__ == "__main__":
    main()
