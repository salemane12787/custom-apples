"""
Build apple armor textures from model.gltf embedded diamond atlases + exact UV layout.
"""
from __future__ import annotations

import base64
import json
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parent.parent
GLTF = Path(r"c:\Users\hp\Desktop\model.gltf")
ASSETS = Path(r"C:\Users\hp\.cursor\projects\d-xnestorio-mode\assets")
HD = ROOT / "src/main/resources/assets/customapples/textures/models/armor/hd"
BB = ROOT / "blockbench"
VANILLA = ROOT / "src/main/resources/assets/customapples/textures/models/armor"
SCALE = 16


def decode_uri(uri: str) -> bytes:
    return base64.b64decode(uri.split(",", 1)[1])


def apple_pixel(r: int, g: int, b: int, a: int) -> tuple[int, int, int, int]:
    if a < 12 or r + g + b < 28:
        return (0, 0, 0, 0)
    lum = (r + g + b) / 3
    if r > 130 and g > 85 and b < 130 and r >= g:
        return (215, 170, 60, a)
    if lum > 175 and r > 140:
        return (255, 215, 100, a)
    if lum < 72:
        return (48, 10, 16, a)
    if g > r and g > 70:
        return (38, 115, 42, a)
    return (min(255, int(r * 1.05)), max(0, int(g * 0.42)), max(0, int(b * 0.42)), a)


def recolor(img: Image.Image) -> Image.Image:
    img = img.convert("RGBA")
    out = Image.new("RGBA", img.size, (0, 0, 0, 0))
    sp, op = img.load(), out.load()
    for y in range(img.height):
        for x in range(img.width):
            op[x, y] = apple_pixel(*sp[x, y])
    return out


def iron_pixel(r: int, g: int, b: int, a: int) -> tuple[int, int, int, int]:
    if a < 12 or r + g + b < 28:
        return (0, 0, 0, 0)
    lum = (r + g + b) / 3
    if lum > 175 and r > 140:
        return (240, 240, 255, a)
    if r > 130 and g > 85:
        return (180, 180, 190, a)
    if lum < 72:
        return (55, 58, 65, a)
    if r > g + 15:
        return (min(255, int(r * 0.9)), int(g * 0.35), int(b * 0.35), a)
    avg = lum
    return (int(min(255, avg * 1.12)), int(min(255, avg * 1.1)), int(min(255, avg * 1.15)), a)


def recolor_iron(img: Image.Image) -> Image.Image:
    img = img.convert("RGBA")
    out = Image.new("RGBA", img.size, (0, 0, 0, 0))
    sp, op = img.load(), out.load()
    for y in range(img.height):
        for x in range(img.width):
            op[x, y] = iron_pixel(*sp[x, y])
    return out


def upscale(img: Image.Image) -> Image.Image:
    w, h = img.size
    return img.resize((w * SCALE, h * SCALE), Image.Resampling.NEAREST)


def save_all(img: Image.Image, hd_names: list[str], vanilla_name: str | None = None) -> None:
    hd = upscale(img)
    for base in (HD, BB):
        base.mkdir(parents=True, exist_ok=True)
        for n in hd_names:
            p = base / n
            hd.save(p)
            print(f"OK {p} ({hd.size[0]}x{hd.size[1]})")
    if vanilla_name:
        VANILLA.mkdir(parents=True, exist_ok=True)
        v = img.resize((img.width * 4, img.height * 4), Image.Resampling.NEAREST)
        vp = VANILLA / vanilla_name
        v.save(vp)
        print(f"OK {vp} ({v.size[0]}x{v.size[1]})")


def main() -> None:
    if not GLTF.exists():
        raise SystemExit(f"Missing {GLTF}")

    gltf = json.loads(GLTF.read_text(encoding="utf-8"))
    images = gltf.get("images", [])
    if len(images) < 2:
        raise SystemExit("GLTF needs 2 embedded texture images")

    layer1 = Image.open(__import__("io").BytesIO(decode_uri(images[0]["uri"])))
    layer2 = Image.open(__import__("io").BytesIO(decode_uri(images[1]["uri"])))

    ASSETS.mkdir(parents=True, exist_ok=True)
    layer1.save(ASSETS / "gltf_diamond_layer1.png")
    layer2.save(ASSETS / "gltf_diamond_layer2.png")

    apple1 = recolor(layer1)
    apple2 = recolor(layer2)
    iron1 = recolor_iron(layer1)

    save_all(
        apple1,
        ["apple_chestplate.png", "apple_armor_texture.png"],
        "apple_layer_1.png",
    )
    save_all(apple2, ["apple_layer_2.png"], "apple_layer_2.png")
    save_all(iron1, ["iron_apple_helmet.png"], "iron_apple_layer_1.png")

    preview = Image.new("RGBA", upscale(apple1).size, (255, 255, 255, 255))
    preview.paste(upscale(apple1), (0, 0), upscale(apple1))
    preview.save(ASSETS / "gltf_apple_texture_preview.png")
    print("Done — apple textures from GLTF diamond UV atlases.")


if __name__ == "__main__":
    main()
