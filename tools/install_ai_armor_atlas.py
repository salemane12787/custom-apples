"""Install AI armor atlases using diamond GLTF UV mask — keeps exact island layout."""
from __future__ import annotations

from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parent.parent
ASSETS = Path(r"C:\Users\hp\.cursor\projects\d-xnestorio-mode\assets")
HD = ROOT / "src/main/resources/assets/customapples/textures/models/armor/hd"
BB = ROOT / "blockbench"
VANILLA = ROOT / "src/main/resources/assets/customapples/textures/models/armor"

W, H = 64, 32
HD_SCALE = 16
VANILLA_SCALE = 4

MASK1 = ASSETS / "gltf_diamond_layer1.png"
MASK2 = ASSETS / "gltf_texture_1.png"
AI1 = ASSETS / "ai_apple_armor_cool_atlas.png"
AI2 = ASSETS / "ai_apple_leggings_cool_atlas.png"


def mask_pixels(img: Image.Image) -> set[tuple[int, int]]:
    img = img.convert("RGBA").resize((W, H), Image.Resampling.NEAREST)
    px = img.load()
    out: set[tuple[int, int]] = set()
    for y in range(H):
        for x in range(W):
            r, g, b, a = px[x, y]
            if a > 20 and r + g + b > 35:
                out.add((x, y))
    return out


def apply_mask(paint: Image.Image, allowed: set[tuple[int, int]]) -> Image.Image:
    paint = paint.convert("RGBA").resize((W, H), Image.Resampling.LANCZOS)
    out = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    sp, op = paint.load(), out.load()
    for y in range(H):
        for x in range(W):
            if (x, y) in allowed:
                op[x, y] = sp[x, y]
    return out


def iron_from_layer1(layer1: Image.Image) -> Image.Image:
    """Helmet UV is top ~40% of layer_1 diamond mask."""
    mask = mask_pixels(Image.open(MASK1))
    helm = {(x, y) for x, y in mask if y < int(H * 0.42)}
    out = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    ip, op = layer1.load(), out.load()
    for x, y in helm:
        r, g, b, a = ip[x, y]
        if a < 20:
            continue
        lum = (r + g + b) / 3
        if lum > 170:
            op[x, y] = (235, 235, 245, a)
        elif r > 130 and g > 90:
            op[x, y] = (175, 175, 185, a)
        elif lum < 75:
            op[x, y] = (50, 53, 60, a)
        elif r > g + 20:
            op[x, y] = (min(255, int(r * 0.85)), int(g * 0.35), int(b * 0.35), a)
        else:
            avg = lum
            op[x, y] = (int(min(255, avg * 1.1)), int(min(255, avg * 1.08)), int(min(255, avg * 1.14)), a)
    return out


def save(img: Image.Image, hd_names: list[str], vanilla_name: str) -> None:
    hd = img.resize((W * HD_SCALE, H * HD_SCALE), Image.Resampling.NEAREST)
    van = img.resize((W * VANILLA_SCALE, H * VANILLA_SCALE), Image.Resampling.NEAREST)
    for base in (HD, BB):
        base.mkdir(parents=True, exist_ok=True)
        for n in hd_names:
            hd.save(base / n)
            print(f"OK {base / n} ({hd.size[0]}x{hd.size[1]})")
    VANILLA.mkdir(parents=True, exist_ok=True)
    van.save(VANILLA / vanilla_name)
    print(f"OK {VANILLA / vanilla_name} ({van.size[0]}x{van.size[1]})")


def main() -> None:
    if not AI1.exists() or not MASK1.exists():
        raise SystemExit("Need ai_apple_armor_cool_atlas.png and gltf_diamond_layer1.png in assets")

    layer1 = apply_mask(Image.open(AI1), mask_pixels(Image.open(MASK1)))
    save(
        layer1,
        ["apple_chestplate.png", "apple_armor_texture.png"],
        "apple_layer_1.png",
    )

    iron = iron_from_layer1(layer1)
    save(iron, ["iron_apple_helmet.png"], "iron_apple_layer_1.png")

    if AI2.exists() and MASK2.exists():
        layer2 = apply_mask(Image.open(AI2), mask_pixels(Image.open(MASK2)))
        save(layer2, ["apple_layer_2.png"], "apple_layer_2.png")

    preview = Image.new("RGBA", (W * HD_SCALE, H * HD_SCALE), (255, 255, 255, 255))
    hd1 = layer1.resize((W * HD_SCALE, H * HD_SCALE), Image.Resampling.NEAREST)
    preview.paste(hd1, (0, 0), hd1)
    preview.save(ASSETS / "ai_apple_armor_installed_preview.png")
    print(f"OK preview {ASSETS / 'ai_apple_armor_installed_preview.png'}")


if __name__ == "__main__":
    main()
