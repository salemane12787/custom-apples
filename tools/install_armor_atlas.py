"""
Place AI armor art on the vanilla 64x32 humanoid UV atlas, then upscale 4x for sharp pixels.

Minecraft always samples UVs at 64x32 coordinates — paint there first, never at scaled coords.
"""
from __future__ import annotations

from pathlib import Path

from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parent.parent
ASSETS = Path(r"C:\Users\hp\.cursor\projects\d-xnestorio-mode\assets")
ARMOR_OUT = ROOT / "src/main/resources/assets/customapples/textures/models/armor"

LAYER1_TEMPLATE = ASSETS / (
    "c__Users_hp_AppData_Roaming_Cursor_User_workspaceStorage_1da29c5ffe78f49bf665a4a0e78eabc5"
    "_images_head_and_chest-a4f79d2c-de05-4921-833a-f9eed309af16.png"
)
LAYER2_TEMPLATE = ASSETS / (
    "c__Users_hp_AppData_Roaming_Cursor_User_workspaceStorage_1da29c5ffe78f49bf665a4a0e78eabc5"
    "_images_legs-694b56f3-4a34-4c89-97ca-40da2f9a064f.png"
)

W, H = 64, 32
UPSCALE = 4


def blank() -> Image.Image:
    return Image.new("RGBA", (W, H), (0, 0, 0, 0))


def upscale(layer: Image.Image) -> Image.Image:
    return layer.resize((W * UPSCALE, H * UPSCALE), Image.Resampling.NEAREST)


def box_faces(u: int, v: int, dx: int, dy: int, dz: int) -> list[tuple[int, int, int, int]]:
    return [
        (u, v + dz, dz, dy),
        (u + dz + dx, v + dz, dz, dy),
        (u + dz, v, dx, dz),
        (u + dz + dx, v, dx, dz),
        (u + dz, v + dz, dx, dy),
        (u + dz + dx + dx, v + dz, dx, dy),
    ]


def remove_magenta(img: Image.Image, tolerance: int = 42) -> Image.Image:
    img = img.convert("RGBA")
    px = img.load()
    for y in range(img.height):
        for x in range(img.width):
            r, g, b, a = px[x, y]
            if abs(r - 255) <= tolerance and g <= tolerance and abs(b - 255) <= tolerance:
                px[x, y] = (r, g, b, 0)
    return img


def load_ai(name: str) -> Image.Image:
    path = ASSETS / name
    if not path.exists():
        raise SystemExit(f"Missing AI asset: {path}")
    img = Image.open(path).convert("RGBA")
    if max(img.size) > 512:
        img.thumbnail((512, 512), Image.Resampling.LANCZOS)
    return remove_magenta(img)


def load_template(path: Path) -> Image.Image:
    img = Image.open(path).convert("RGBA")
    if img.size != (W, H):
        img = img.resize((W, H), Image.Resampling.NEAREST)
    return img


def prepare_material(material: Image.Image, size: int = 32) -> Image.Image:
    w, h = material.size
    side = min(w, h)
    x0, y0 = (w - side) // 2, (h - side) // 2
    crop = material.crop((x0, y0, x0 + side, y0 + side))
    return crop.resize((size, size), Image.Resampling.LANCZOS)


def tile_material(material: Image.Image, tw: int, th: int) -> Image.Image:
    mat = prepare_material(material)
    mw, mh = mat.size
    tile = Image.new("RGBA", (tw, th), (0, 0, 0, 0))
    mp, tp = mat.load(), tile.load()
    for py in range(th):
        for px in range(tw):
            cr, cg, cb, ca = mp[px % mw, py % mh]
            if ca > 8:
                tp[px, py] = (cr, cg, cb, ca)
    return tile


def fill_rects(layer: Image.Image, rects: list[tuple[int, int, int, int]], material: Image.Image) -> None:
    for x, y, rw, rh in rects:
        tile = tile_material(material, rw, rh)
        layer.paste(tile, (x, y), tile)


def clip_template(layer: Image.Image, template: Image.Image) -> Image.Image:
    out = blank()
    lp, op, tp = layer.load(), out.load(), template.load()
    for y in range(H):
        for x in range(W):
            tr, tg, tb, ta = tp[x, y]
            lr, lg, lb, la = lp[x, y]
            if ta >= 12 and tr + tg + tb >= 18 and la > 8:
                op[x, y] = (lr, lg, lb, la)
    return out


def overlay_rect(layer: Image.Image, rect: tuple[int, int, int, int], overlay: Image.Image, fill: float = 0.9) -> None:
    x, y, rw, rh = rect
    ow, oh = max(1, int(rw * fill)), max(1, int(rh * fill))
    tile = overlay.resize((ow, oh), Image.Resampling.LANCZOS)
    layer.paste(tile, (x + (rw - ow) // 2, y + (rh - oh) // 2), tile)


def add_boot_studs(layer: Image.Image, bottoms: list[tuple[int, int, int, int]]) -> None:
    draw = ImageDraw.Draw(layer)
    for x, y, rw, rh in bottoms:
        if rh > 6:
            continue
        for py in range(y + 1, y + rh - 1):
            for px in [x + 1, x + rw - 2]:
                draw.point((px, py), fill=(235, 200, 45, 255))


def opaque_count(img: Image.Image) -> int:
    return sum(1 for y in range(img.height) for x in range(img.width) if img.getpixel((x, y))[3] > 10)


def save_pair(name: str, layer1: Image.Image, layer2: Image.Image | None = None) -> None:
    ARMOR_OUT.mkdir(parents=True, exist_ok=True)
    out1 = upscale(layer1)
    out1.save(ARMOR_OUT / f"{name}_layer_1.png")
    print(f"OK {name}_layer_1.png {out1.size} ({opaque_count(out1)} px)")
    if layer2 is not None:
        out2 = upscale(layer2)
        out2.save(ARMOR_OUT / f"{name}_layer_2.png")
        print(f"OK {name}_layer_2.png {out2.size} ({opaque_count(out2)} px)")


# Vanilla humanoid UV regions (64x32 logical coords)
HELMET = box_faces(0, 0, 8, 8, 8)
CHEST = box_faces(16, 16, 8, 12, 4) + box_faces(40, 16, 4, 12, 4)
CHEST_FRONT = (20, 20, 8, 12)
BOOTS = box_faces(0, 16, 4, 12, 4)
BOOT_BOTTOM = [(u + 4, v, 4, 4) for u, v, _, rh in box_faces(0, 16, 4, 12, 4) if rh == 4]
LEGGINGS = box_faces(16, 16, 8, 12, 4) + box_faces(0, 16, 4, 12, 4)


def build(rects: list[tuple[int, int, int, int]], material: Image.Image,
          template: Image.Image | None = None) -> Image.Image:
    layer = blank()
    fill_rects(layer, rects, material)
    if template is not None:
        layer = clip_template(layer, template)
    return layer


def paste_layer(name: str, layer: Image.Image) -> None:
    ARMOR_OUT.mkdir(parents=True, exist_ok=True)
    out = upscale(layer)
    out.save(ARMOR_OUT / name)
    print(f"OK {name} {out.size} ({opaque_count(out)} px)")


def main() -> None:
    tpl1 = load_template(LAYER1_TEMPLATE)
    tpl2 = load_template(LAYER2_TEMPLATE)

    iron_mat = load_ai("ai_armor_iron_material.png")
    red_mat = load_ai("ai_armor_red_material.png")
    legs_mat = load_ai("ai_armor_legs_material.png")
    boots_mat = load_ai("ai_armor_boots_material.png")
    chest_overlay = load_ai("ai_chest_apple_overlay.png")

    # Iron apple = helmet only → iron_apple_layer_1.png
    iron = build(HELMET, iron_mat, tpl1)
    paste_layer("iron_apple_layer_1.png", iron)

    # Apple set = ONE layer_1 (chest + boots) and ONE layer_2 (leggings)
    layer1 = blank()
    chest = build(CHEST, red_mat, tpl1)
    overlay_rect(chest, CHEST_FRONT, chest_overlay, 0.88)
    layer1.paste(chest, (0, 0), chest)
    boots = build(BOOTS, boots_mat, tpl1)
    add_boot_studs(boots, BOOT_BOTTOM)
    layer1.paste(boots, (0, 0), boots)
    paste_layer("apple_layer_1.png", layer1)

    leggings = build(LEGGINGS, legs_mat, tpl2)
    paste_layer("apple_layer_2.png", leggings)

    print("Done — apple_layer_1 + apple_layer_2 (full set), iron_apple_layer_1 (helmet).")


if __name__ == "__main__":
    main()
